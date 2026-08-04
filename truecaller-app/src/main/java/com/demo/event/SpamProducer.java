package com.demo.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpamProducer {

    private final KafkaTemplate<String, SpamReportEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.spam-report}")
    private String topic;

    public void publish(SpamReportEvent event) {
        kafkaTemplate.send(topic, event.getPhoneNumber(), event)
                .whenComplete((result, ex) -> {
                    if(ex != null){
                        log.error("Failed to send spam report for number={}", event.getPhoneNumber(), ex);
                       // throw new RuntimeException("Failed to send spam report event", ex);
                    }
                    else{
                        log.info("Spam report event sent for number={}", event.getPhoneNumber());
                    }
                });
    }
}
