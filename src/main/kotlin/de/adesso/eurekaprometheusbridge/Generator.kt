package de.adesso.eurekaprometheusbridge

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class Generator(
        @Value("\${bridge.scrapeinterval}") var scrape_interval: Int = 15,
        @Value("\${bridge.scrapetimeout}") var scrape_timeout: Int = 10,
        @Value("\${bridge.metricspath}") var metrics_path: String = "/prometheus",
        @Value("\${bridge.scheme}") var scheme: String = "http") {

    companion object {
        //With using a companion object the logger isnt created for each class instance, this is for best pratice purposes
        val log = LoggerFactory.getLogger(Generator::class.java.name)
    }

    fun generatePrometheusConfig(entries: List<ConfigEntry>, generatedFilePath: String) {
        var template = File("src/main/resources/prometheus-basic.yml").readText()
        for (configEntry in entries) {
            var entry = """
- job_name: ${configEntry.name}
  scrape_interval: ${scrape_interval}s
  scrape_timeout: ${scrape_timeout}s
  metrics_path: $metrics_path
  scheme: $scheme
  static_configs:
  - targets:
    - ${configEntry.targeturl}
                """.trimIndent()
            template += "\n" + entry
        }
        var file = File(generatedFilePath + "prometheus.yml")
        file.writeText(template)
        log.info("Config generated!")
    }

}