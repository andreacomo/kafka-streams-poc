package it.codingjam.poc.kafkastreamspoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafkaStreams

@SpringBootApplication
@EnableKafkaStreams
class KafkaStreamsPocApplication

fun main(args: Array<String>) {
	runApplication<KafkaStreamsPocApplication>(*args)
}
