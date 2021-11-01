package it.codingjam.poc.kafkastreamspoc.listeners.dtos

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class CredentialDTO(
    val id: Long,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String
) {

    companion object {
        fun createFrom(value: JsonNode): CredentialDTO {
            val payload = value["payload"]["after"]
            return CredentialDTO(
                payload["id"].asLong(),
                payload["client_id"].asText(),
                payload["client_secret"].asText()
            )
        }
    }
}
