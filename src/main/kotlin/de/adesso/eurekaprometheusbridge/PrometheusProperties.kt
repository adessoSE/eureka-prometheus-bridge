package de.adesso.eurekaprometheusbridge

import io.github.konfigur8.ConfigurationTemplate
import io.github.konfigur8.Property

object PrometheusProperties {
    var scrapeInterval = Property.int("bridge.prometheus.scrapeinterval")
    var scrapeTimeout = Property.int("bridge.prometheus.scrapetimeout")
    var metricsPath = Property.string("bridge.prometheus.metricspath")
    var scheme = Property.string("bridge.prometheus.scheme")
    var configFileTemplatePath = Property.string("bridge.prometheus.configFileTemplatePath")
    var generatedConfigFilePath = Property.string("bridge.prometheus.generatedConfigFilePath")
    var testConfigFilePath = Property.string("bridge.prometheus.testConfigFilePath")

    val configTemplate = ConfigurationTemplate()
            .withProp(scrapeInterval, 15)
            .withProp(scrapeTimeout, 10)
            .withProp(metricsPath, "/eureka/apps/")
            .withProp(scheme, "http")
            .withProp(configFileTemplatePath, "/prometheus-basic.yml")
            .withProp(generatedConfigFilePath, "dockerfile-prometheus/generated-prometheus-configs/prometheus.yml")
            .withProp(testConfigFilePath, "/prometheus-test.yml")
}