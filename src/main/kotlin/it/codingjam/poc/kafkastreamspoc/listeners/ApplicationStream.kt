package it.codingjam.poc.kafkastreamspoc.listeners

//import it.codingjam.poc.kafkastreamspoc.serdes.JsonDeserializer
//import it.codingjam.poc.kafkastreamspoc.serdes.JsonSerializer
import com.fasterxml.jackson.databind.JsonNode
import it.codingjam.poc.kafkastreamspoc.listeners.dtos.ApplicationDTO
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.connect.json.JsonDeserializer
import org.apache.kafka.connect.json.JsonSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
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
        val dtoSerde = JsonSerde(ApplicationDTO::class.java)
        var jacksonSerde = Serdes.serdeFrom(JsonSerializer(), JsonDeserializer())

        streamsBuilder.stream("postgres.public.applications", Consumed.with(jacksonSerde, jacksonSerde))
            .filter { _, value -> value["payload"]["op"].asText() == "c" }
            .map { _, value -> KeyValue(getId(value), getApplicationDTO(value)) }
            .peek { key, value -> logger.info("{} -> {}", key, value) }
            .to("new.application", Produced.with(longSerde, dtoSerde))
    }

    private fun getApplicationDTO(value: JsonNode): ApplicationDTO {
        val payload = value["payload"]["after"]
        return ApplicationDTO(
            payload["id"].asLong(),
            payload["name"].asText(),
            payload["credentials_id"].asLong()
        )
    }

    private fun getId(value: JsonNode) = value["payload"]["after"]["id"].asLong()

}