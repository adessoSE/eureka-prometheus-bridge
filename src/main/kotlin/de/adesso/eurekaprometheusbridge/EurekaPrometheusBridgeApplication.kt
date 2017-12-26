package de.adesso.eurekaprometheusbridge

import khttp.get
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.json.XML




@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
}


@Component
class ScheduledClass {
    var eureka_standard_url = "http://127.0.0.1:8761"


    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka(): Boolean {
        println("""
            |----------------------------------------------|
            |Now querying Eureka                           |
            |----------------------------------------------|
        """.trimMargin())
        val r = get(eureka_standard_url + "/eureka/apps/")

        if(r.statusCode == 200) {
            println("""
            |----------------------------------------------|
            |Successfully found Eureka-Clients             |
            |----------------------------------------------|
        """.trimMargin())
            println("Status: " + r.statusCode)
            //Convert xml tto JSON
            val xmlJSONObj = XML.toJSONObject(r.text)
            val jsonPrettyPrintString = xmlJSONObj.toString(4)
            println(""""
                ${jsonPrettyPrintString}
                """)
            //TODO JSON to Prometheusformat
            return true
        }
        println("""
            |----------------------------------------------|
            |No Eureka-Clients found                       |
            |----------------------------------------------|
            Status: ${r.statusCode}
            Text:
            ${XML.toJSONObject(r.text).toString(4)}
            """)
        return false
    }
}