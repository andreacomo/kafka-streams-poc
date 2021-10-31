package it.codingjam.poc.kafkastreamspoc.models.entities

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "applications")
class Application(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    var name: String,

    @OneToOne(cascade = [CascadeType.ALL])
    var credentials: Credential

) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Application) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}