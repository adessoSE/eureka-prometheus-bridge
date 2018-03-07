package de.adesso.eurekaprometheusbridge

import com.google.common.io.CharStreams
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.File
import java.io.InputStreamReader

@Service
class Generator {

    companion object {
        //With using a companion object the logger isnt created for each class instance, this is for best pratice purposes
        val log = LoggerFactory.getLogger(this::class.java.name)
        var config = PrometheusProperties.configTemplate.reify()
    }

    fun generatePrometheusConfig(entries: List<ConfigEntry>) {
        log.info("Reading basic Prometheusfile")
       // var template = File(config.get(PrometheusProperties.configFileTemplatePath)).readText()

        //Check Config-Template existing
        var resource = ClassPathResource(config[PrometheusProperties.configFileTemplatePath]) //null if empty
        var resourceInputStream = resource.inputStream
        var template = CharStreams.toString(InputStreamReader(resourceInputStream, "UTF-8"))
        if(resource == null){
            log.error("RESOURCE NULL")
        }

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