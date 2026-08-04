package com.demo.repository;

import com.demo.model.mongo.ContactDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactDocumentRepository extends MongoRepository<ContactDocument, String> {
}
