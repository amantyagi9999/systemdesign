package com.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.topics.spam-report}")
    private String spamReportTopic;

    @Bean
    public NewTopic spamReportTopic(){
        return TopicBuilder.name(spamReportTopic)
                .partitions(4)
                .replicas(1)
                .build();
    }
}
