package it.codingjam.poc.kafkastreamspoc.listeners

import com.fasterxml.jackson.databind.JsonNode
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationWithCredentialDTO
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.CredentialDTO
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.StreamJoined
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
 * Using join between streams in a time window frame
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

        val newApplicationStream = streamsBuilder.stream("postgres.public.applications", Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getCredentialId(value), toApplicationDTO(value)) }
            .peek { key, value -> logger.info("{} -> {}", key, value) }

        val newCredentialStream = streamsBuilder.stream("postgres.public.credentials", Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getId(value), toCredentialDTO(value)) }
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
            .to("new.application.windowed", Produced.with(longSerde, JsonSerde(ApplicationWithCredentialDTO::class.java)))
    }

    private fun isCreated(value: JsonNode) = value["payload"]["op"].asText() == "c"

    private fun toApplicationDTO(value: JsonNode): ApplicationDTO {
        val payload = value["payload"]["after"]
        return ApplicationDTO(
            payload["id"].asLong(),
            payload["name"].asText(),
            payload["credentials_id"].asLong()
        )
    }

    private fun toCredentialDTO(value: JsonNode): CredentialDTO {
        val payload = value["payload"]["after"]
        return CredentialDTO(
            payload["id"].asLong(),
            payload["client_id"].asText(),
            payload["client_secret"].asText()
        )
    }

    private fun getId(value: JsonNode): Long = value["payload"]["after"]["id"].asLong()

    private fun getCredentialId(value: JsonNode): Long = value["payload"]["after"]["credentials_id"].asLong()

}