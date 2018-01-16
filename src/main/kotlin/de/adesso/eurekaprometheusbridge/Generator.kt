package de.adesso.eurekaprometheusbridge

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

@Service
class Generator(
        @Autowired var prometheus: PrometheusProperties
) {

    companion object {
        //With using a companion object the logger isnt created for each class instance, this is for best pratice purposes
        val log = LoggerFactory.getLogger(Generator::class.java.name)
    }

    fun generatePrometheusConfig(entries: List<ConfigEntry>) {
        log.info("Reading basic Prometheusfile")
        var template = File(prometheus.configFileTemplatePath).readText()
        for (configEntry in entries) {
            var entry = """
- job_name: ${configEntry.name}
  scrape_interval: ${prometheus.scrapeInterval}s
  scrape_timeout: ${prometheus.scrapeTimeout}s
  metrics_path: ${prometheus.metricsPath}
  scheme: ${prometheus.scheme}
  static_configs:
  - targets:
    - ${configEntry.targeturl}
                """.trimIndent()
            template += "\n" + entry
        }
        var file = File(prometheus.generatedConfigFilePath)
        file.writeText(template)
        log.info("Config generated!")
    }

}