package com.demo.event;

import com.demo.dto.enums.SpamCategory;
import com.demo.model.dynamo.ResolvedNumber;
import com.demo.repository.ResolvedNumberDynamoRepository;
import com.demo.service.ResolvedNumberCacheService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpamConsumer {

    private static final double SPAM_LABEL_THRESHOLD = 0.6;
    private static final double SCORE_INCREMENT_PER_REPORT = 0.05;

    private final ResolvedNumberDynamoRepository resolvedNumberDynamoRepository;
    private final ResolvedNumberCacheService resolvedNumberCacheService;

    @KafkaListener(topics = "${spring.kafka.topics.spam-report}")
    public void consume(SpamReportEvent spamReportEvent) {
        log.info("Spam report received for number={} category={}", spamReportEvent.getPhoneNumber(), spamReportEvent.getCategory());
        try{
            ResolvedNumber existingNumber = resolvedNumberDynamoRepository.findByPhoneNumber(spamReportEvent.getPhoneNumber())
                    .orElseGet(() -> ResolvedNumber.builder()
                            .phoneNumber(spamReportEvent.getPhoneNumber())
                            .resolvedName("UNKNOWN")
                            .category(SpamCategory.NONE.name()) // works as SPAM LEVEL
                            .spamScore(0.0)
                            .businessVerified(false)
                            .build());
            double newScore = Math.min(1.0, existingNumber.getSpamScore() != null ? existingNumber.getSpamScore() + SCORE_INCREMENT_PER_REPORT : 0.0);
            String newCategory = newScore >= SPAM_LABEL_THRESHOLD ? spamReportEvent.getCategory().name() : existingNumber.getCategory();

            ResolvedNumber updated = existingNumber.toBuilder()
                    .spamScore(newScore)
                    .category(newCategory)
                    .lastUpdated(Instant.now())
                    .build();

            resolvedNumberDynamoRepository.save(updated);
            resolvedNumberCacheService.evict(spamReportEvent.getPhoneNumber());

            log.info("Processed spam report for number={} newScore={} newLabel={}", spamReportEvent.getPhoneNumber(), newScore, newCategory);
        }
        catch (Exception ex) {
            // Kafka will redeliver on consumer failure per the configured
            // ack mode / retry policy - log and let it retry rather than
            // silently swallowing the event.
            log.error("Failed to process spam report event for number={}", spamReportEvent.getPhoneNumber(), ex);
            throw ex;
        }




    }



}
