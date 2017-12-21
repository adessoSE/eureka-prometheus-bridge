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
@EnableDiscoveryClient
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
    var s = ScheduledClass()
    s.registerWitthEureka()
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

/**
private class DefaultDataCenterInfo private constructor(val name: Name) : DataCenterInfo {
    companion object {
        fun myOwn(): DataCenterInfo {
            return DefaultDataCenterInfo(Name.MyOwn)
        }
    }
}*/
class ScheduledClass {
    var eureka_standard_url = "http://localhost:8761"

    var json_register = """
{
    "instance":
    {
        "hostName":"localhost",
        "app":"eurekaprometheusbridge",
        "ipAddr":"localhost",
        "vipAddress":"1111",
        "secureVipAddress":"1111",
        "status":"STARTING",
        "port": { "${'$'}": 1111, "@enabled": "true" },
        "dataCenterInfo": { "name": "MyOwn" }
    }
}
    """.trimIndent()

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

    fun registerWitthEureka() {
        println("Now registering with Eureka")

        put(eureka_standard_url + "/eureka/v2/apps/eurekaprometheusbridge/1", json = json_register)
    }
}

data class EurekaApp(
        var id: Long?,
        var name: String?
)
