package de.adesso.eurekaprometheusbridge

import io.github.konfigur8.Configuration
import io.github.konfigur8.Property
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.FileNotFoundException
import javax.annotation.PostConstruct
import java.io.InputStreamReader
import org.springframework.core.io.ClassPathResource
import com.google.common.io.CharStreams

@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication {
    companion object {
        val log = LoggerFactory.getLogger(this::class.java.name)
        var eureka_config = EurekaProperties.configTemplate.reify()
        var prometheus_config = PrometheusProperties.configTemplate.reify()
    }

    @PostConstruct
    fun logConfigurationParameters() {
        //Check Config-Template existing
        var resource = ClassPathResource(prometheus_config.get(PrometheusProperties.configFileTemplatePath)) //null if empty
        if(!resource.exists()){
            throw FileNotFoundException("The configFileTemplate wasn't found under: " + PrometheusProperties.configFileTemplatePath +
                    "\n The App can't start and will shut down.")        }
        else {
            var resourceInputStream = resource.inputStream
            var string = CharStreams.toString(InputStreamReader(resourceInputStream, "UTF-8"))
            log.info("The configFileTemplate was found on the classpath under: " + prometheus_config.get(PrometheusProperties.configFileTemplatePath))
            //log.info(string)
        }
        //Log all Parameters and Values (config_template exiting - throught konfigur8 secured)
        log.info("-------------- Initial Eureka-Properties --------------------------------")
        logConfigParameter(EurekaProperties.port, eureka_config)
        logConfigParameter(EurekaProperties.host, eureka_config)
        logConfigParameter(EurekaProperties.apiPath, eureka_config)
        logConfigParameter(EurekaProperties.showJson, eureka_config)
        log.info("-------------- Initial Eureka-Properties end --------------------------------")

        //Log all Parameters and Values (config_template exiting - throught konfigur8 secured)
        log.info("-------------- Initial Prometheus-Properties --------------------------------")
        logConfigParameter(PrometheusProperties.scrapeInterval, prometheus_config)
        logConfigParameter(PrometheusProperties.scrapeTimeout, prometheus_config)
        logConfigParameter(PrometheusProperties.metricsPath, prometheus_config)
        logConfigParameter(PrometheusProperties.scheme, prometheus_config)
        logConfigParameter(PrometheusProperties.configFileTemplatePath, prometheus_config)
        logConfigParameter(PrometheusProperties.generatedConfigFilePath, prometheus_config)
        log.info("-------------- Initial Prometheus-Properties end --------------------------------")
    }

    fun logConfigParameter(prop: Property<*>, config: Configuration) {
        log.info(prop.toString() + " \t Value: " + config.valueOf(prop).toString())
    }
}

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
}