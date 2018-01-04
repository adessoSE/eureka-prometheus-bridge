package de.adesso.eurekaprometheusbridge

import khttp.get
import khttp.responses.Response
import org.json.JSONException
import org.json.JSONObject
import org.json.XML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
}


@Service
class ScheduledClass(
        @Autowired var configRepo: ConfigEntryRepository,
        @Value("\${bridge.eureka.port}") var eureka_port: String,
        @Value("\${bridge.eureka.host}") var eureka_host: String,
        @Value("\${bridge.filePath}") var generated_file_path: String) {

    @PostConstruct
    fun init() {
        configRepo.deleteAll()
    }

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka() {
        println("Query Eureka ...")
        var r: Response?
        try {
            r = get(eureka_host + ":" + eureka_port + "/eureka/apps/")
        } catch (e: Exception) {
            println("Requesting Eureka failed!... Trying again in some time.")
            return
        }
        if (r.statusCode == 200) {
            println("Found Eureka")
            println("Status: " + r.statusCode)
            //Convert xml tto JSON
            val JSONObjectFromXML = XML.toJSONObject(r.text)
            val jsonPrettyPrintString = JSONObjectFromXML.toString(4)
            println(""""
                ${jsonPrettyPrintString}
                """)
            //If JSON is too short no app is registered
            if(JSONObjectFromXML.toString().length < 60){
                println("JSON too short, no app registered with eureka.")
            return
            }

            //Is it one object or an array?
            var isArray = false
            try {
                if (JSONObjectFromXML.getJSONObject("applications").getJSONObject("application") is JSONObject) {
                    isArray = false

                }
            } catch (e: JSONException) {
                isArray = true
            }

            if (!isArray) {
                var name = JSONObjectFromXML.getJSONObject("applications").getJSONObject("application").get("name")
                var hostname = JSONObjectFromXML.getJSONObject("applications").getJSONObject("application").getJSONObject("instance").get("hostName")
                var port = JSONObjectFromXML.getJSONObject("applications").getJSONObject("application").getJSONObject("instance").getJSONObject("port").get("content")
                var targeturl = (hostname.toString() + ":" + port.toString())
                println("""Found property
                Name: $name
                Targeturl: $targeturl
                """.trimIndent())
            }
            else if (isArray) {
                println("Found multiple Objects:")
                for (o in JSONObjectFromXML.getJSONObject("applications").getJSONArray("application")) {
                    if (o is JSONObject) {

                        var name = o.get("name")
                        var hostname = o.getJSONObject("instance").get("hostName")
                        var port = o.getJSONObject("instance").getJSONObject("port").get("content")
                        var targeturl = (hostname.toString() + ":" + port.toString())

                        println(""" Found Service: $name with targeturl: $targeturl
                            """.trimIndent())

                        var nameFound = !configRepo.findByName(name.toString()).isEmpty()
                        var urlFound = !configRepo.findByTargeturl(targeturl).isEmpty()
                        if (!nameFound && !urlFound) {
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        } else if (nameFound && !urlFound) {
                            configRepo.deleteByName(name.toString())
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        } else if (!nameFound && urlFound) {
                            configRepo.deleteByTargeturl(targeturl)
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        }
                    }
                }
            }
        } else {
            println("""No Eureka-Clients found
            Status: ${r.statusCode}
            Text:
            ${XML.toJSONObject(r.text).toString(4)}
            """)
        }
    }

    /**Attempts to generate a new Config-File*/
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    fun generateConfigFile() {
        println("Generate Config File ...")

        var gen = Generator()
        println("All Entries in Database:")
        for (e in configRepo.findAll()) {
            println(e.toString())
        }
        gen.generatePrometheusConfig(configRepo.findAll(), generated_file_path)
    }

}

@Service
class Generator(
        @Value("\${bridge.scrapeinterval}") var scrape_interval: Int = 15,
        @Value("\${bridge.scrapetimeout}") var scrape_timeout: Int = 10,
        @Value("\${bridge.metricspath}") var metrics_path: String = "/prometheus",
        @Value("\${bridge.scheme}") var scheme: String = "http") {

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

    fun generatePrometheusConfig(entries: List<ConfigEntry>, generatedFilePath: String) {
        var template = basic_config
        for (configEntry in entries) {
            var entry = """
- job_name: ${configEntry.name}
  scrape_interval: ${scrape_interval}s
  scrape_timeout: ${scrape_timeout}s
  metrics_path: ${metrics_path}
  scheme: http
  static_configs:
  - targets:
    - ${configEntry.targeturl}
                """.trimIndent()
            template += "\n" + entry;
        }
        var file: File = File(generatedFilePath + "prometheus.yml")
        file.writeText(template)
    }

}

@Entity
data class ConfigEntry(
        @Id @GeneratedValue var id: Long? = null,
        val name: String = "",
        val targeturl: String = "") {
}

interface ConfigEntryRepository : JpaRepository<ConfigEntry, Long> {
    override fun findAll(sort: Sort?): MutableList<ConfigEntry>
    fun findByTargeturl(targeturl: String): List<ConfigEntry>
    fun findByName(name: String): List<ConfigEntry>
    fun deleteByName(name: String)
    fun deleteByTargeturl(targeturl: String)
}



