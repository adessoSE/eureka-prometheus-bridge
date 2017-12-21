package de.adesso.eurekaprometheusbridge

import khttp.get
import khttp.put
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
    var s = ScheduledClass()
    s.queryEureka()
}


@RestController
internal class ServiceInstanceRestController {

    @Autowired
    val discoveryClient: DiscoveryClient? = null

    @RequestMapping("/service-instances/{applicationName}")
    fun serviceInstancesByApplicationName(
            @PathVariable applicationName: String): List<ServiceInstance> {
        return this.discoveryClient!!.getInstances(applicationName)
    }
}

class ScheduledClass {
    var eureka_standard_url = "http://localhost:8761"

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka(): Boolean {
        println("Now querying Eureka")
        val r = get(eureka_standard_url + "/eureka/v2/apps")
        println("""
            Status: ${r.statusCode}
            Text: ${r.text}
            """)
        return true
    }

}