package it.codingjam.poc.kafkastreamspoc.resources

import it.codingjam.poc.kafkastreamspoc.models.entities.Application
import it.codingjam.poc.kafkastreamspoc.models.entities.Credential
import it.codingjam.poc.kafkastreamspoc.services.ApplicationService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/applications",
    produces = [MediaType.APPLICATION_JSON_VALUE],
    consumes = [MediaType.APPLICATION_JSON_VALUE])
class ApplicationResource(val applicationService: ApplicationService) {

    @PostMapping
    fun create(@RequestBody application: Application): Application {
        return applicationService.saveNew(application)
    }

    @PutMapping("/{appId}/credentials")
    fun updateCredentials(@PathVariable("appId") appId: Long,
                          @RequestBody credential: Credential): Application {
        return applicationService.update(credential, appId)
    }

    @GetMapping
    fun getAll(): List<Application> {
        return applicationService.getAll()
    }
}