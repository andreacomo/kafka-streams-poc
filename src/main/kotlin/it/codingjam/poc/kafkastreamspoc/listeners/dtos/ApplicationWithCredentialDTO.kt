package it.codingjam.poc.kafkastreamspoc.listeners.dtos

data class ApplicationWithCredentialDTO(
    val id: Long,
    val name: String,
    val clientId: String,
    val clientSecret: String
)
