package it.codingjam.poc.kafkastreamspoc.services

import it.codingjam.poc.kafkastreamspoc.models.entities.Application
import it.codingjam.poc.kafkastreamspoc.models.entities.Credential
import it.codingjam.poc.kafkastreamspoc.repositories.ApplicationRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class ApplicationService(val applicationRepository: ApplicationRepository) {

    fun getAll(): List<Application> {
        return applicationRepository.findAll()
    }

    fun saveNew(application: Application): Application {
        return applicationRepository.save(application)
    }

    @Transactional
    fun update(credential: Credential, applicationId: Long): Application {
        val application = applicationRepository.findById(applicationId)
            .orElseThrow()
        application.credentials.clientId = credential.clientId
        application.credentials.clientSecret = credential.clientSecret

        return applicationRepository.save(application)
    }
}