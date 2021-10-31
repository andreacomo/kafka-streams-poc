package it.codingjam.poc.kafkastreamspoc.models.entities

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "credentials")
class Credential(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var clientId: String,

    var clientSecret: String

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Credential) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
