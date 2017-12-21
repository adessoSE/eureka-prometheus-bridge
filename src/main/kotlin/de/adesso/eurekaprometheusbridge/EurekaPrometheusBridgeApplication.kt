package de.adesso.eurekaprometheusbridge

import khttp.get
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
    var s = ScheduledClass()
}

class ScheduledClass {
    var eureka_standard_url = "http://localhost:8761/eureka/v2/apps"

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka(): Boolean {
        val r = get(eureka_standard_url)
        print(r)
        return true
    }
}

data class EurekaApp(
        var id: Long?,
        var name: String?
)
