package de.adesso.eurekaprometheusbridge

import khttp.get
import khttp.put
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
    s.registerWitthEureka()
    s.queryEureka()
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
    "instance": {
        "hostName": "WKS-SOF-L011",
        "app": "com.automationrhapsody.eureka.app",
        "vipAddress": "com.automationrhapsody.eureka.app",
        "secureVipAddress": "com.automationrhapsody.eureka.app"
        "ipAddr": "10.0.0.10",
        "status": "STARTING",
        "port": {"${'$'}": "8080", "@enabled": "true"},
        "securePort": {"${'$'}": "8443", "@enabled": "true"},
        "healthCheckUrl": "http://WKS-SOF-L011:8080/healthcheck",
        "statusPageUrl": "http://WKS-SOF-L011:8080/status",
        "homePageUrl": "http://WKS-SOF-L011:8080",
        "dataCenterInfo": {
            "@class": "com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo",
            "name": "MyOwn"
        },
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

        put(eureka_standard_url + "/eureka/v2/apps/eurekaprometheusbridge-1234567890",json = json_register)
    }
}

data class EurekaApp(
        var id: Long?,
        var name: String?
)
