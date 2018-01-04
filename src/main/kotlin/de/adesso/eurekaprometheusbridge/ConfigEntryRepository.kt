package de.adesso.eurekaprometheusbridge

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConfigEntryRepository : JpaRepository<ConfigEntry, Long> {
    override fun findAll(sort: Sort?): MutableList<ConfigEntry>
    fun findByTargeturl(targeturl: String): List<ConfigEntry>
    fun findByName(name: String): List<ConfigEntry>
    fun deleteByName(name: String)
    fun deleteByTargeturl(targeturl: String)
}