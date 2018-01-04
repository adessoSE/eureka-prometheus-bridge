package de.adesso.eurekaprometheusbridge

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class ConfigEntry(
        @Id @GeneratedValue var id: Long? = null,
        val name: String = "",
        val targeturl: String = "") {
}