package de.adesso.eurekaprometheusbridge

import io.github.konfigur8.ConfigurationTemplate
import io.github.konfigur8.Property

object PrometheusProperties {
    var group = "bridge.prometheus"
    var scrapeInterval = Property.int(group + "scrapeinterval")
    var scrapeTimeout = Property.int(group + "scrapetimeout")
    var metricsPath = Property.string(group + "metricspath")
    var scheme  = Property.string(group + "scheme")
    var configFileTemplatePath  = Property.string(group + "configFileTemplatePath")
    var generatedConfigFilePath  = Property.string(group + "generatedConfigFilePath")

    val configTemplate = ConfigurationTemplate()
            .withProp(scrapeInterval, 15)
            .withProp(scrapeTimeout,  10)
            .withProp(metricsPath,  "/eureka/apps/")
            .withProp(scheme,  "http")
            .withProp(configFileTemplatePath,  "src/main/resources/prometheus-basic.yml")
            .withProp(generatedConfigFilePath,  "generated-prometheus-configs/prometheus.yml")
}