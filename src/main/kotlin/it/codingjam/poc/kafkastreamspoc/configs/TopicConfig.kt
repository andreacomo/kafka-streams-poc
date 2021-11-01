package it.codingjam.poc.kafkastreamspoc.configs

import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_APPLICATIONS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_CREDENTIALS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.NEW_APPLICATION_TOPIC_TABLED
import it.codingjam.poc.kafkastreamspoc.configs.Topic.NEW_APPLICATION_TOPIC_WINDOWED
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class TopicConfig {

    @Bean
    fun createNewApplicationTopic(): NewTopic = newTopic(NEW_APPLICATION_TOPIC_TABLED)

    @Bean
    fun createNewApplicationWindowedTopic(): NewTopic = newTopic(NEW_APPLICATION_TOPIC_WINDOWED)

    // anticipates following Debezium topics creation in order to attach streams on startup

    @Bean
    fun createCdcApplicationsTopic(): NewTopic = newTopic(CDC_APPLICATIONS)

    @Bean
    fun createCdcCredentialsTopic(): NewTopic = newTopic(CDC_CREDENTIALS)

    private fun newTopic(name: String) = TopicBuilder
            .name(name)
            .partitions(1)
            .replicas(1)
            .build()
}