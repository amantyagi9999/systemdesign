package com.demo.repository;

import com.demo.model.mongo.ContactDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMongoRepository extends MongoRepository<ContactDocument, String> {
}
