package de.adesso.eurekaprometheusbridge

import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class EurekaPrometheusBridgeApplicationTests {

    @Test
    fun loadConfigTemplates() {
        var eureka_config = EurekaProperties.configTemplate.reify()
        var prometheus_config = PrometheusProperties.configTemplate.reify()

        assertEquals(8761, eureka_config.valueOf(EurekaProperties.port))
        assertEquals("http://127.0.0.1", eureka_config.valueOf(EurekaProperties.host))
        assertEquals("/eureka/apps/", eureka_config.valueOf(EurekaProperties.apiPath))
        assertEquals(false, eureka_config.valueOf(EurekaProperties.showJson))

        assertEquals(15, prometheus_config.valueOf(PrometheusProperties.scrapeInterval))
        assertEquals(10, prometheus_config.valueOf(PrometheusProperties.scrapeTimeout))
        assertEquals("/eureka/apps/", prometheus_config.valueOf(PrometheusProperties.metricsPath))
        assertEquals("http", prometheus_config.valueOf(PrometheusProperties.scheme))
        assertEquals("src/main/resources/prometheus-basic.yml", prometheus_config.valueOf(PrometheusProperties.configFileTemplatePath))
        assertEquals("generated-prometheus-configs/prometheus.yml", prometheus_config.valueOf(PrometheusProperties.generatedConfigFilePath))
    }

}
