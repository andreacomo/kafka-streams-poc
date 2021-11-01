package it.codingjam.poc.kafkastreamspoc.listeners

//import it.codingjam.poc.kafkastreamspoc.serdes.JsonDeserializer
//import it.codingjam.poc.kafkastreamspoc.serdes.JsonSerializer
import com.fasterxml.jackson.databind.JsonNode
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
import org.apache.kafka.streams.kstream.ValueJoiner
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.LoggerFactory
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ApplicationStream(val streamsBuilder: StreamsBuilder) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun processStream() {
        val longSerde = Serdes.Long()
        val jacksonSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

        val newApplicationTable = streamsBuilder.stream("postgres.public.applications", Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getId(value), toApplicationDTO(value)) }
            .peek { key, value -> logger.info("{} -> {}", key, value) }
            .toTable(Materialized.`as`<Long, ApplicationDTO, KeyValueStore<Bytes, ByteArray>>("NEW-APPLICATION-TABLE")
                .withKeySerde(longSerde)
                .withValueSerde(JsonSerde(ApplicationDTO::class.java))
            )

        val newCredentialTable = streamsBuilder.stream("postgres.public.credentials", Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> isCreated(value) }
            .map { _, value -> KeyValue(getId(value), toCredentialDTO(value)) }
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
            .to("new.application", Produced.with(longSerde, JsonSerde(ApplicationWithCredentialDTO::class.java)))
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

}