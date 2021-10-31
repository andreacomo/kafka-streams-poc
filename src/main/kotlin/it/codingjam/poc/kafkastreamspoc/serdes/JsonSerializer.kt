package it.codingjam.poc.kafkastreamspoc.serdes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class JsonSerializer<T> : Serializer<T> {

    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        objectMapper.setTimeZone(TimeZone.getDefault())
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addDeserializer(
            LocalDateTime::class.java,
            LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME)
        )
        objectMapper.registerModule(javaTimeModule)
    }

    override fun serialize(topic: String?, data: T): ByteArray? {
        return if (data == null) {
            null
        } else try {
            objectMapper.writeValueAsBytes(data)
        } catch (e: IOException) {
            throw SerializationException("Error serializing message", e)
        }

    }
}