package de.adesso.eurekaprometheusbridge

import io.github.konfigur8.ConfigurationTemplate
import io.github.konfigur8.Property

object EurekaProperties {
    var port = Property.int("eureka.bridge.port")
    var host = Property.string("eureka.bridge.host")
    var apiPath  = Property.string("eureka.bridge.apiPath")
    var showJson  = Property.bool("eureka.bridge.showJson")
    
    val configTemplate = ConfigurationTemplate()
            .withProp(port, 8761)
            .withProp(host,  "http://127.0.0.1")
            .withProp(apiPath,  "/eureka/apps/")
            .withProp(showJson,  false)
}