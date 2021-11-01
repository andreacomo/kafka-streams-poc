package it.codingjam.poc.kafkastreamspoc.listeners.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class CredentialDTO(
    val id: Long,
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String
)
