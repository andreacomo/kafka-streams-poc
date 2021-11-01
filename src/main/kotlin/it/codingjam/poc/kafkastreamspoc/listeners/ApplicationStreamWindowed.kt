package it.codingjam.poc.kafkastreamspoc.listeners

import com.fasterxml.jackson.databind.JsonNode
import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_APPLICATIONS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.CDC_CREDENTIALS
import it.codingjam.poc.kafkastreamspoc.configs.Topic.NEW_APPLICATION_TOPIC_WINDOWED
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationWithCredentialDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.CredentialDTO
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

/**
 * When changing profile, clean up temp folder (/private/tmp/kafka-streams on Mac)
 *
 * Using join between streams in a time window frame.
 * <br>
 * Using global table to manage credentials <strong>update</strong>
 */
@Component
@Qualifier("applicationStream")
@Profile("windowed")
class ApplicationStreamWindowed(val streamsBuilder: StreamsBuilder) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun processStream() {
        val longSerde = Serdes.Long()
        val jacksonSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())
        val applicationWithCredentialsSerde = JsonSerde(ApplicationWithCredentialDTO::class.java)

        val newCredentialStream = onCreateApplication(jacksonSerde, longSerde, applicationWithCredentialsSerde)

        onUpdateApplicationCredentials(longSerde, applicationWithCredentialsSerde, newCredentialStream)
    }

    private fun onUpdateApplicationCredentials(
        longSerde: Serde<Long>,
        applicationWithCredentialsSerde: JsonSerde<ApplicationWithCredentialDTO>,
        newCredentialStream: KStream<Long, CredentialDTO>
    ) {
        val applicationWithCredential = streamsBuilder.globalTable(NEW_APPLICATION_TOPIC_WINDOWED, Consumed.with(longSerde, applicationWithCredentialsSerde))

        val joinOnUpdate = newCredentialStream.join(
            applicationWithCredential,
            { key, _ -> key },
            { cred, app ->
                ApplicationWithCredentialDTO(
                    app.id,
                    app.name,
                    cred.clientId,
                    cred.clientSecret
                )
            }
        )

        joinOnUpdate.peek { key, value -> logger.info("{} -> {}", key, value) }
            .to(NEW_APPLICATION_TOPIC_WINDOWED, Produced.with(longSerde, applicationWithCredentialsSerde))
    }

    private fun onCreateApplication(
        jacksonSerde: Serde<JsonNode>,
        longSerde: Serde<Long>,
        applicationWithCredentialsSerde: JsonSerde<ApplicationWithCredentialDTO>
    ): KStream<Long, CredentialDTO> {
        val newApplicationStream =
            streamsBuilder.stream(CDC_APPLICATIONS, Consumed.with(jacksonSerde, jacksonSerde))
                .filter { _, value -> isCreated(value) }
                .map { _, value -> KeyValue(getCredentialId(value), ApplicationDTO.createFrom(value)) }
                .peek { key, value -> logger.info("{} -> {}", key, value) }

        val newCredentialStream =
            streamsBuilder.stream(CDC_CREDENTIALS, Consumed.with(jacksonSerde, jacksonSerde))
                .map { _, value -> KeyValue(getId(value), CredentialDTO.createFrom(value)) }
                .peek { key, value -> logger.info("{} -> {}", key, value) }

        val join = newApplicationStream.join(
            newCredentialStream,
            { app, cred ->
                ApplicationWithCredentialDTO(
                    app.id,
                    app.name,
                    cred.clientId,
                    cred.clientSecret
                )
            },
            JoinWindows.of(Duration.ofSeconds(3)),
            StreamJoined.`as`<Long?, ApplicationDTO?, CredentialDTO?>("JOIN")
                .withKeySerde(longSerde)
                .withValueSerde(JsonSerde(ApplicationDTO::class.java))
                .withOtherValueSerde(JsonSerde(CredentialDTO::class.java))
        )

        join.peek { key, value -> logger.info("{} -> {}", key, value) }
            .to(NEW_APPLICATION_TOPIC_WINDOWED, Produced.with(longSerde, applicationWithCredentialsSerde))
        return newCredentialStream
    }

    private fun isCreated(value: JsonNode) = value["payload"]["op"].asText() == "c"

    private fun getId(value: JsonNode): Long = value["payload"]["after"]["id"].asLong()

    private fun getCredentialId(value: JsonNode): Long = value["payload"]["after"]["credentials_id"].asLong()

}