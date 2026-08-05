package com.demo.repository;

import com.demo.model.mongo.VerifiedBusinessDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedBusinessMongoRepository extends MongoRepository<VerifiedBusinessDocument, String> {
}
