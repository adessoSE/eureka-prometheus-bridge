package de.adesso.eurekaprometheusbridge

import com.google.gson.JsonArray
import khttp.get
import org.json.JSONArray
import org.json.JSONObject
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
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json




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
            val JSONObj = XML.toJSONObject(r.text)
            val jsonPrettyPrintString = JSONObj.toString(4)
            println(""""
                ${jsonPrettyPrintString}
                """)

            /**
             *
             *
             * ACHTUNG, das Parsing ist noch nicht für mehrere Services gedacht, eine effiziente Library für Kotlin wäre schön
             * 
             *
             */
            //TODO Lösung für mehrere Services
            println("Object: " + JSONObj.get("applications"))
            var name = JSONObj.getJSONObject("applications").getJSONObject("application").get("name")
            var hostname = JSONObj.getJSONObject("applications").getJSONObject("application").getJSONObject("instance").get("hostName")
            var port = JSONObj.getJSONObject("applications").getJSONObject("application").getJSONObject("instance").getJSONObject("port").get("content")
            println("""
                $name
                $hostname
                $port
            """.trimIndent())

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
