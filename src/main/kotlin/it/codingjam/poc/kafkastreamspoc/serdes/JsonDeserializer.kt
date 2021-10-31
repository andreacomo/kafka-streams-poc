package it.codingjam.poc.kafkastreamspoc.serdes

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class JsonDeserializer<T>(private val forType: Class<T>) : Deserializer<T> {

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

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        return if (data == null) {
            null
        } else try {
            objectMapper.readValue(data, forType)
        } catch (e: IOException) {
            throw SerializationException(e)
        }
    }
}