package de.adesso.eurekaprometheusbridge

import khttp.get
import org.json.JSONArray
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
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import java.io.File
import javax.annotation.PostConstruct
import javax.persistence.*


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
        @Value("\${bridge.eureka.port}") var eureka_port: String = "8761",
        @Value("\${bridge.eureka.host}") var eureka_host: String = "http://127.0.0.1",
        @Value("\${bridge.filePath}")  var generated_file_path: String) {

        var eureka_standard_url = eureka_host + ":" + eureka_port


    @PostConstruct
    fun init(){
        configRepo.deleteAll()
    }

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka() {
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
            val JSONObjectFromXML = XML.toJSONObject(r.text)
            val jsonPrettyPrintString = JSONObjectFromXML.toString(4)
            println(""""
                ${jsonPrettyPrintString}
                """)

            //TODO Check .getJSONArray if Array, otherwise use singlevalue implementation
            for (o in JSONObjectFromXML.getJSONObject("applications").getJSONArray("application")) {
                if (o is JSONObject) {
                    var name = o.get("name")
                    if(o.getJSONObject("instance") is JSONObject){

                    }
                    else if (o.getJSONArray("instance") is JSONArray){

                    }
                    var hostname = o.getJSONObject("instance").get("hostName")
                    var port = o.getJSONObject("instance").getJSONObject("port").get("content")
                    println("""
                            |----------------------------------------------|
                            |Found Properties                              |
                            |----------------------------------------------|
                            $name
                            $hostname
                            $port
                            |----------------------------------------------|
                            |Saving ConfigEntry                            |
                            |----------------------------------------------|
                            """.trimIndent())


                    var targeturl = (hostname.toString() + ":" + port.toString())

                    /**var configEntryList = configRepo.findByTargeturl(targeturl)

                    var targetList: ArrayList<String> = ArrayList()
                    for(entry in configEntryList){
                        targetList.add(entry.targeturl)
                    }*/



                        var nameFound = !configRepo.findByName(name.toString()).isEmpty()
                        var urlFound = !configRepo.findByTargeturl(targeturl).isEmpty()


                        if(!nameFound && !urlFound){
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        }
                        else if(nameFound && !urlFound){
                            configRepo.deleteByName(name.toString())
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        }
                        else if(!nameFound && urlFound){
                            configRepo.deleteByTargeturl(targeturl)
                            configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                            continue
                        }

                    /**if(!targetList.contains(targeturl)) {
                        configRepo.save(ConfigEntry(name = name.toString(), targeturl = targeturl))
                    }*/

                        /**
                    entryList.add(ConfigEntry(name = name.toString(), targeturl = (hostname.toString() + ":" + port.toString())))
                    for (entr in entryList){
                        println("EntryList")
                        println(entr.toString())
                    }*/
                }
            }
        }
        else {
            println("""
            |----------------------------------------------|
            |No Eureka-Clients found                       |
            |----------------------------------------------|
            Status: ${r.statusCode}
            Text:
            ${XML.toJSONObject(r.text).toString(4)}
            """)
        }
    }

    /**Attempts to generate a new Config-File*/
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    fun generateConfigFile() {
        println("""
            |----------------------------------------------|
            |Generate ConfigFile                           |
            |----------------------------------------------|
            """.trimIndent())

        var gen = Generator()
        println("""
            |----------------------------------------------|
            |ConfigRepo Findall                           |
            |----------------------------------------------|
            """.trimIndent())
        for(e in configRepo.findAll()){
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
        @Value("\${bridge.scheme}") var scheme: String = "http"){

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
        for (configEntry in entries){
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
        val targeturl: String = ""){

}

@Controller
class ConfigEntryController(
        @Autowired val repo: ConfigEntryRepository) {

}

interface ConfigEntryRepository : JpaRepository <ConfigEntry,Long> {

    override fun findAll(sort: Sort?): MutableList<ConfigEntry>
    fun findByTargeturl(targeturl: String): List<ConfigEntry>
    fun findByName(name: String): List<ConfigEntry>
    fun deleteByName(name: String)
    fun deleteByTargeturl(targeturl: String)
}



