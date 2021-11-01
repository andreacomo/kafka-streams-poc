package it.codingjam.poc.kafkastreamspoc.listeners.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class ApplicationDTO(
    val id: Long,
    val name: String,
    @JsonProperty("credentials_id")
    val credentialsId: Long
    ) {

    companion object {
        fun createFrom(value: JsonNode): ApplicationDTO {
            val payload = value["payload"]["after"]
            return ApplicationDTO(
                payload["id"].asLong(),
                payload["name"].asText(),
                payload["credentials_id"].asLong()
            )
        }
    }
}
