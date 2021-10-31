package it.codingjam.poc.kafkastreamspoc.configs

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class TopicConfig {

    @Bean
    fun createNewApplicationTopic(): NewTopic = TopicBuilder
        .name("new.application")
        .partitions(1)
        .replicas(1)
        .build()
}