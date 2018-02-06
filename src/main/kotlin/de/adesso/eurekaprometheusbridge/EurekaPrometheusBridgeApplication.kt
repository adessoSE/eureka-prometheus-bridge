package de.adesso.eurekaprometheusbridge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class EurekaPrometheusBridgeApplication

fun main(args: Array<String>) {
    runApplication<EurekaPrometheusBridgeApplication>(*args)
}



