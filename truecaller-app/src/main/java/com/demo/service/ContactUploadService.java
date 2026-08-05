package com.demo.service;

import com.demo.dto.ContactDto;
import com.demo.dto.ContactUploadDto;
import com.demo.exception.DownstreamUnavailableException;
import com.demo.model.mongo.ContactDocument;
import com.demo.model.mongo.ContactEntry;
import com.demo.repository.ContactMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactUploadService {

    private final ContactMongoRepository contactMongoRepository;

    public int uploadContacts(ContactUploadDto contactUploadDto) {
        List<ContactEntry> contactDtoList = contactUploadDto.getContacts().stream()
                .map(this::toEntity)
                .toList();

        ContactDocument contactDocument = ContactDocument.builder()
                .contacts(contactDtoList)
                .uploadedAt(Instant.now())
                .userId(contactUploadDto.getUserId())
                .build();

        try{
            contactMongoRepository.delete(contactDocument);
            contactDocument = contactMongoRepository.save(contactDocument);
            return contactDocument.getContacts().size();
        } catch (DataAccessException ex) {
            log.error("MongoDB write failed for userId={}", contactUploadDto.getUserId(), ex);
            throw new DownstreamUnavailableException("MongoDB", ex);
        }

    }


    private ContactEntry toEntity(ContactDto dto) {
        return ContactEntry.builder()
                .phoneNumber(dto.getPhoneNumber())
                .savedName(dto.getName())
                .photoUrl(dto.getPhoto())
                .build();
    }
}
