package it.codingjam.poc.kafkastreamspoc.listeners.dtos

import com.fasterxml.jackson.annotation.JsonProperty

data class ApplicationDTO(
    var id: Long,
    var name: String,
    @JsonProperty("credentials_id")
    var credentialsId: Long
    )
