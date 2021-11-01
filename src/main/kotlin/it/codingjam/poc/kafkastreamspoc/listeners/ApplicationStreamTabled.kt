package it.codingjam.poc.kafkastreamspoc.listeners

import com.fasterxml.jackson.databind.JsonNode
import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_APPLICATIONS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_CREDENTIALS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.NEW_APPLICATION_TOPIC_TABLED
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationWithCredentialDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.CredentialDTO
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * When changing profile, clean up temp folder (/private/tmp/kafka-streams on Mac)
 *
 * Using join between tables
 */
@Component
@Qualifier("applicationStream")
@Profile("tabled")
class ApplicationStreamTabled(val streamsBuilder: StreamsBuilder) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun processStream() {
        val longSerde = Serdes.Long()
        val jacksonSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

        val newApplicationTable = streamsBuilder.stream(CDC_APPLICATIONS, Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getId(value), ApplicationDTO.createFrom(value)) }
            .peek { key, value -> logger.info("{} -> {}", key, value) }
            .toTable(Materialized.`as`<Long, ApplicationDTO, KeyValueStore<Bytes, ByteArray>>("NEW-APPLICATION-TABLE")
                .withKeySerde(longSerde)
                .withValueSerde(JsonSerde(ApplicationDTO::class.java))
            )

        val newCredentialTable = streamsBuilder.stream(CDC_CREDENTIALS, Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getId(value), CredentialDTO.createFrom(value)) }
            .peek { key, value -> logger.info("{} -> {}", key, value) }
            .toTable(Materialized.`as`<Long, CredentialDTO, KeyValueStore<Bytes, ByteArray>>("NEW-CREDENTIAL-TABLE")
                .withKeySerde(longSerde)
                .withValueSerde(JsonSerde(CredentialDTO::class.java))
            )

        val join = newApplicationTable.join(
            newCredentialTable,
            ApplicationDTO::credentialsId
        ) { app, cred ->
            ApplicationWithCredentialDTO(
                app.id,
                app.name,
                cred.clientId,
                cred.clientSecret
            )
        }

        join.toStream()
            .peek { key, value -> logger.info("{} -> {}", key, value) }
            .to(NEW_APPLICATION_TOPIC_TABLED, Produced.with(longSerde, JsonSerde(ApplicationWithCredentialDTO::class.java)))
    }

    private fun isCreated(value: JsonNode) = value["payload"]["op"].asText() == "c"

    private fun getId(value: JsonNode): Long = value["payload"]["after"]["id"].asLong()

}