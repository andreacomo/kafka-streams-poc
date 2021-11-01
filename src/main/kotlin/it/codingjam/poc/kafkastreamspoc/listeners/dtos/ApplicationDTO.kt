package it.codingjam.poc.kafkastreamspoc.listeners.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationDTO(
    val id: Long,
    val name: String,
    @JsonProperty("credentials_id")
    val credentialsId: Long
    )
