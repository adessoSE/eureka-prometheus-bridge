package de.adesso.eurekaprometheusbridge

import junit.framework.Assert.assertEquals
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import javax.annotation.Resource

@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
class EurekaPrometheusBridgeApplicationTests {

    @Resource
    lateinit var eurekaQuery: EurekaQuery

    @Resource
    lateinit var generator: Generator

    @Resource
    lateinit var scheduledJobs: ScheduledJobs

    @Test
    fun testConfigTemplates() {
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

    @Test
    fun testConfigEntry() {
        var testEntry = ConfigEntry(name = "TestEntry", targeturl = "http://localhost:1234")
        assertEquals("TestEntry", testEntry.name)
        assertEquals("http://localhost:1234", testEntry.targeturl)
    }

    @Test
    fun testGenerator() {
        var configEntries = ArrayList<ConfigEntry>()
        configEntries.add(ConfigEntry("TestEntry1", "http://localhost:1001"))
        configEntries.add(ConfigEntry("TestEntry2", "http://localhost:1002"))
        generator.generatePrometheusConfig(configEntries)
        assertEquals(File(Generator.config.get(PrometheusProperties.testConfigFilePath)).readText(),
                File(Generator.config.get(PrometheusProperties.generatedConfigFilePath)).readText())
    }

    @Test
    fun testEurekaQuery() {
        eurekaQuery.queryEureka()
    }

    @Test
    fun testScheduledJobs() {
        scheduledJobs.executeBridge()
    }

}

@Aspect
@Component
open class TracingAspect {

    @Pointcut("execution(* de.adesso.eurekaprometheusbridge.*.*(..))")
    open fun testMethods() {
    }

    @Around("testMethods()")
    open fun before(joinPoint: ProceedingJoinPoint) {
        val log = LoggerFactory.getLogger(ScheduledJobs::class.java.name)
        val start = System.currentTimeMillis()
        log.info("Going to call the method: " + joinPoint.signature.name)
        val output = joinPoint.proceed()
        log.info("Method execution completed: " + joinPoint.signature.name)
        val elapsedTime = System.currentTimeMillis() - start
        log.info("Method execution time: $elapsedTime milliseconds for: " + joinPoint.signature.name)
    }
}
