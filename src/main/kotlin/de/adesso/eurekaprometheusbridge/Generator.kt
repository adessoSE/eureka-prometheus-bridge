package de.adesso.eurekaprometheusbridge

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class Generator {

    companion object {
        //With using a companion object the logger isnt created for each class instance, this is for best pratice purposes
        val log = LoggerFactory.getLogger(Generator::class.java.name)
        var config = PrometheusProperties.configTemplate.reify()
    }

    fun generatePrometheusConfig(entries: List<ConfigEntry>) {
        log.info("Reading basic Prometheusfile")
        var template = File(config[PrometheusProperties.configFileTemplatePath]).readText()
        for (configEntry in entries) {
            var entry = """
- job_name: ${configEntry.name}
  scrape_interval: ${config[PrometheusProperties.scrapeInterval]}s
  scrape_timeout: ${config[PrometheusProperties.scrapeTimeout]}s
  metrics_path: ${config[PrometheusProperties.metricsPath]}
  scheme: ${config[PrometheusProperties.scheme]}
  static_configs:
  - targets:
    - ${configEntry.targeturl}
                """.trimIndent()
            template += "\n" + entry
        }
        var file = File(config[PrometheusProperties.generatedConfigFilePath])
        file.writeText(template)
        log.info("Config generated!")
    }

}