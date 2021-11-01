package it.codingjam.poc.kafkastreamspoc.configs

object Topic {

    const val NEW_APPLICATION_TOPIC_WINDOWED = "new.application.windowed"

    const val NEW_APPLICATION_TOPIC_TABLED = "new.application.tabled"

    const val CDC_APPLICATIONS = "postgres.public.applications"

    const val CDC_CREDENTIALS = "postgres.public.credentials"
}