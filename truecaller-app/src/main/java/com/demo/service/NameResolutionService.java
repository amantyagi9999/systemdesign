package com.demo.service;

import com.demo.model.mongo.ContactDocument;
import com.demo.model.mongo.ContactEntry;
import com.demo.model.mongo.UserDocument;
import com.demo.model.mongo.VerifiedBusinessDocument;
import com.demo.repository.ContactMongoRepository;
import com.demo.repository.ResolvedNumberDynamoRepository;
import com.demo.repository.UserMongoRepository;
import com.demo.repository.VerifiedBusinessMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * In-process reimplementation of the trust-weighted, whole-string-frequency
 * name-resolution algorithm - runs inside the Spring Boot app itself
 * (triggered via NameResolutionJobTriggerService / AdminJobController), no
 * Spark cluster or separate jar required.
 *
 * Algorithm recap (same as designed in the interview):
 *  1. Whole-string frequency, not token-level - every vote is the full
 *     saved-name string, counted atomically, to avoid stitching together a
 *     name nobody actually saved.
 *  2. Trust-weighted, not raw count - each contributor's vote is weighted
 *     by TrustScoreCalculator (account age, phone verification, upload
 *     velocity), defending against botnets of freshly-created accounts.
 *  3. Business verification override - if a number matches the verified
 *     business registry, that name wins outright.
 *  4. Spam fields are never touched - only writes resolvedName /
 *     businessVerified / photoUrl via ResolvedNumberDynamoRepository's
 *     partial UpdateItem, leaving spamLabel/spamScore (owned by
 *     SpamReportConsumer) untouched.
 *
 * TRADEOFF vs a Spark-based job: this loads all contacts into JVM memory
 * and aggregates with plain HashMaps, which is simple and requires no
 * extra infrastructure, but doesn't horizontally scale the way a
 * distributed batch framework would for a true billions-of-rows dataset.
 * For a demo / moderate-scale deployment this is fine and a lot easier to
 * operate; if the "contacts" collection grows to the scale discussed in
 * the interview's capacity estimation (100M+ users), you'd want to revisit
 * a distributed approach (Spark, or a MongoDB aggregation pipeline that
 * pushes the grouping down to the database).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NameResolutionService {

    private final ContactMongoRepository contactMongoRepository;
    private final UserMongoRepository userMongoRepository;
    private final VerifiedBusinessMongoRepository verifiedBusinessMongoRepository;
    private final ResolvedNumberDynamoRepository resolvedNumberDynamoRepository;

    /**
     * Runs the full resolution pass synchronously (caller decides whether
     * to invoke this on a background thread - see NameResolutionJobTriggerService).
     *
     * @return number of phone numbers resolved and written to DynamoDB
     */
    public int run() {
        long startTime = System.currentTimeMillis();
        log.info("Name resolution job starting");

        Map<String, Double> trustScores = loadTrustScores();
        Map<String, String> verifiedBusinesses = loadVerifiedBusinesses();

        // number -> (name -> accumulated trust weight)
        Map<String, Map<String, Double>> nameWeights = new HashMap<>();
        // number -> first non-null photo seen
        Map<String, String> photos = new HashMap<>();

        long contactDocsProcessed = 0;
        long votesProcessed = 0;

        for (ContactDocument doc : contactMongoRepository.findAll()) {
            contactDocsProcessed++;
            double trust = trustScores.getOrDefault(doc.getUserId(),
                    com.truecaller.app.batch.TrustScoreCalculator.compute(null, false, 0, Instant.now()));

            if (doc.getContacts() == null) {
                continue;
            }

            // One vote per (userId, number) even if a single upload had
            // accidental duplicate rows for the same contact.
            Set<String> seenNumbersInThisDoc = new HashSet<>();

            for (ContactEntry entry : doc.getContacts()) {
                if (entry.getPhoneNumber() == null || entry.getSavedName() == null || entry.getSavedName().isBlank()) {
                    continue;
                }
                if (!seenNumbersInThisDoc.add(entry.getPhoneNumber())) {
                    continue; // duplicate within the same upload - skip
                }

                nameWeights
                        .computeIfAbsent(entry.getPhoneNumber(), k -> new HashMap<>())
                        .merge(entry.getSavedName(), trust, Double::sum);

                if (entry.getPhotoUrl() != null) {
                    photos.putIfAbsent(entry.getPhoneNumber(), entry.getPhotoUrl());
                }
                votesProcessed++;
            }
        }

        log.info("Processed {} contact documents, {} votes, {} unique numbers",
                contactDocsProcessed, votesProcessed, nameWeights.size());

        int written = 0;
        for (Map.Entry<String, Map<String, Double>> numberEntry : nameWeights.entrySet()) {
            String phoneNumber = numberEntry.getKey();

            String winningName = pickWinningName(numberEntry.getValue());
            boolean businessVerified = verifiedBusinesses.containsKey(phoneNumber);
            if (businessVerified) {
                winningName = verifiedBusinesses.get(phoneNumber);
            }

            resolvedNumberDynamoRepository.updateNameFields(
                    phoneNumber, winningName, businessVerified, photos.get(phoneNumber));
            written++;
        }

        long elapsedMs = System.currentTimeMillis() - startTime;
        log.info("Name resolution job complete - {} numbers resolved in {} ms", written, elapsedMs);
        return written;
    }

    private String pickWinningName(Map<String, Double> namesForNumber) {
        return namesForNumber.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }

    private Map<String, Double> loadTrustScores() {
        Instant now = Instant.now();
        Map<String, Double> scores = new HashMap<>();
        for (UserDocument user : userMongoRepository.findAll()) {
            double score = com.truecaller.app.batch.TrustScoreCalculator.compute(
                    user.getAccountCreatedAt(), user.isPhoneVerified(), user.getUploadsInLast24h(), now);
            scores.put(user.getUserId(), score);
        }
        return scores;
    }

    private Map<String, String> loadVerifiedBusinesses() {
        Map<String, String> businesses = new HashMap<>();
        for (VerifiedBusinessDocument business : verifiedBusinessMongoRepository.findAll()) {
            businesses.put(business.getPhoneNumber(), business.getBusinessName());
        }
        return businesses;
    }
}

