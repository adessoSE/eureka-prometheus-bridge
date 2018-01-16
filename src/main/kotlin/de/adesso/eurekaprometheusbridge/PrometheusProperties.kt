package de.adesso.eurekaprometheusbridge

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bridge.prometheus")
data class PrometheusProperties(
        var scrapeInterval: Int = Integer.valueOf(15),
        var scrapeTimeout: Int = Integer.valueOf(10),
        var metricsPath: String = "/prometheus",
        var scheme: String = "http",
        var configFileTemplatePath: String = "",
        var generatedConfigFilePath: String = "")