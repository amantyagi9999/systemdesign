package com.demo.service;

import com.demo.dto.SpamDto;
import com.demo.event.SpamProducer;
import com.demo.event.SpamReportEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpamReportService {

    private final SpamProducer spamProducer;

    public void reportSpam(SpamDto spamDto){

        SpamReportEvent spamEvent = SpamReportEvent.builder()
                .phoneNumber(spamDto.getPhoneNumber())
                .category(spamDto.getCategory())
                .reportedBy(spamDto.getReportedBy())
                .reportedAt(LocalDateTime.now())
                .build();

        spamProducer.publish(spamEvent);
        log.info("Spam report accepted for number={} category={}", spamDto.getPhoneNumber(), spamDto.getCategory());
    }


}
