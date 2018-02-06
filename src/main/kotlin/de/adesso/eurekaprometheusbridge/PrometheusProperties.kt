package de.adesso.eurekaprometheusbridge

import io.github.konfigur8.ConfigurationTemplate
import io.github.konfigur8.Property

object PrometheusProperties {
    var scrapeInterval = Property.int("bridge.prometheus.scrapeinterval")
    var scrapeTimeout = Property.int("bridge.prometheus.scrapetimeout")
    var metricsPath = Property.string("bridge.prometheus.metricspath")
    var scheme  = Property.string("bridge.prometheus.scheme")
    var configFileTemplatePath  = Property.string("bridge.prometheus.configFileTemplatePath")
    var generatedConfigFilePath  = Property.string("bridge.prometheus.generatedConfigFilePath")

    val configTemplate = ConfigurationTemplate()
            .withProp(scrapeInterval, 15)
            .withProp(scrapeTimeout,  10)
            .withProp(metricsPath,  "/eureka/apps/")
            .withProp(scheme,  "http")
            .withProp(configFileTemplatePath,  "src/main/resources/prometheus-basic.yml")
            .withProp(generatedConfigFilePath,  "generated-prometheus-configs/prometheus.yml")
}