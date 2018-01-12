package de.adesso.eurekaprometheusbridge

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bridge.eureka")
data class EurekaProperties(
        var port: Int = Integer.valueOf(8761),
        var host: String = "http://127.0.0.1",
        var apiPath: String = "/eureka/apps/",
        var showJson: Boolean = false)