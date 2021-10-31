package it.codingjam.poc.kafkastreamspoc.repositories;

import it.codingjam.poc.kafkastreamspoc.models.entities.Application
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ApplicationRepository : JpaRepository<Application, Long>