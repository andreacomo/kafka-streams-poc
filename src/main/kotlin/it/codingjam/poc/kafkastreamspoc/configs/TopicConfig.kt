package it.codingjam.poc.kafkastreamspoc.configs

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class TopicConfig {

    @Bean
    fun createNewApplicationTopic(): NewTopic = TopicBuilder
        .name("new.application.tabled")
        .partitions(1)
        .replicas(1)
        .build()

    @Bean
    fun createNewApplicationWindowedTopic(): NewTopic = TopicBuilder
        .name("new.application.windowed")
        .partitions(1)
        .replicas(1)
        .build()

    // anticipates following Debezium topics creation in order to attach streams on startup

    @Bean
    fun createCdcApplicationsTopic(): NewTopic = TopicBuilder
        .name("postgres.public.applications")
        .partitions(1)
        .replicas(1)
        .build()

    @Bean
    fun createCdcCredentialsTopic(): NewTopic = TopicBuilder
        .name("postgres.public.credentials")
        .partitions(1)
        .replicas(1)
        .build()
}