package de.adesso.eurekaprometheusbridge

import khttp.get
import khttp.responses.Response
import org.json.JSONException
import org.json.JSONObject
import org.json.XML
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class ScheduledJobs(
        @Autowired var configRepo: ConfigEntryRepository,
        @Value("\${bridge.eureka.port}") var eureka_port: String,
        @Value("\${bridge.eureka.host}") var eureka_host: String,
        @Value("\${bridge.filePath}") var generated_file_path: String,
        @Value("\${bridge.show.eurekajson}") var show_eureka_json: Boolean
        ) {

    companion object {
        val log = LoggerFactory.getLogger(ScheduledJobs::class.java.name)
    }

    @PostConstruct
    fun init() {
        configRepo.deleteAll()
    }

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka() {
        log.info("Query Eureka ...")
        var r: Response?
        try {
            r = get(eureka_host + ":" + eureka_port + "/eureka/apps/")
        } catch (e: Exception) {
            log.info("Requesting Eureka failed!... Trying again in some time.")
            return
        }
        if (r.statusCode == 200) {
            log.info("Found Eureka")
            log.info("Status: " + r.statusCode)
            //Convert xml tto JSON
            val JSONObjectFromXML = XML.toJSONObject(r.text)
            if(show_eureka_json) {
                val jsonPrettyPrintString = JSONObjectFromXML.toString(4)
                println(""""
                ${jsonPrettyPrintString}
                """)
            }
            //If JSON is too short no app is registered
            if(JSONObjectFromXML.toString().length < 60){
                log.error("JSON too short, no app registered with eureka.")
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
                log.info("""Found property
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

                        log.info(""" Found Service: $name with targeturl: $targeturl
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
            log.error("""No Eureka-Clients found
            Status: ${r.statusCode}
            Text:
            ${XML.toJSONObject(r.text).toString(4)}
            """)
        }
    }

    /**Attempts to generate a new Config-File*/
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    fun generateConfigFile() {
        log.info("Generating Config File ...")

        var gen = Generator()
        log.info("All Entries in Database:")
        for (e in configRepo.findAll()) {
            println(e.toString())
        }
        gen.generatePrometheusConfig(configRepo.findAll())
    }

}