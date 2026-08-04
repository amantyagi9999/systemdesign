package com.demo.service;

import com.demo.dto.SearchDto;
import com.demo.model.dynamo.ResolvedNumber;
import com.demo.repository.ResolvedNumberDynamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ResolvedNumberDynamoRepository resolvedNumberDynamoRepository;
    public SearchDto searchPhoneNumber(String phoneNumber) {
        ResolvedNumber resolvedNumber = resolvedNumberDynamoRepository.findByPhoneNumber(phoneNumber).orElse(null);
        return SearchDto.builder()
                .displayName(resolvedNumber.getResolvedName())
                .businessVerified(resolvedNumber.getBusinessVerified())
                .photoUrl(resolvedNumber.getPhotoUrl())
                .category(resolvedNumber.getCategory())
                .build();
    }
}
