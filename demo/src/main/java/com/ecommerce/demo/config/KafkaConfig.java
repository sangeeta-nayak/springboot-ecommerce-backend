package com.ecommerce.demo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    public NewTopic ordersTopic(){
        return TopicBuilder
                .name("orders")
                .partitions(3)
                .replicas(1)
                .build();

    }

}
