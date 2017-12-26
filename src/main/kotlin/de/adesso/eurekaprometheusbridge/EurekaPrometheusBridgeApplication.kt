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
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File


@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
}


@Service
class ScheduledClass {

    @Value("\${bridge.eureka.port}")
    var eureka_port: String = "8761"

    var eureka_host: String = "http://127.0.0.1:"
    var eureka_standard_url = eureka_host + eureka_port


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
            //TODO Scrape Name and URL as ConfigEntry
            




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

@Service
class Generator{
    var basic_config: String = """
global:
    scrape_interval: 15s
    scrape_timeout: 10s
    evaluation_interval: 15s
alerting:
  alertmanagers:
  - static_configs:
    - targets: []
    scheme: http
    timeout: 10s
scrape_configs:
- job_name: prometheus
  scrape_interval: 15s
  scrape_timeout: 10s
  metrics_path: /metrics
  scheme: http
  static_configs:
  - targets:
    - localhost:9090
    """.trimIndent()

    @Value("\${bridge.scrapeinterval}")
    var scrape_interval = 15

    @Value("\${bridge.scrapetimeout}")
    var scrape_timeout = 10

    @Value("\${bridge.metricspath}")
    var metrics_path = "/prometheus"

    @Value("\${bridge.scheme}")
    var scheme = "http"

    var target: String? = null

    fun generatePrometheusConfig(entries: List<ConfigEntry>){
        var template = ""
        for (configEntry in entries){
            template = """
                - job_name: ${configEntry.name}
                    scrape_interval: ${scrape_interval}s
                    scrape_timeout: ${scrape_timeout}s
                    metrics_path: $metrics_path
                    scheme: http
                    static_configs:
                        - targets:
                            - ${configEntry.targeturl}
                """.trimIndent()
            template + " " + configEntry;
        }
        var file: File = File("src/generated-prometheus-config/prometheus.yml")
        file.writeText(template)
    }

}

data class ConfigEntry(val name: String, val targeturl: String)
