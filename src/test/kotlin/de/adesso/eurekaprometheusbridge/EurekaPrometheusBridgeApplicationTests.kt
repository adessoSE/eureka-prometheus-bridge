package de.adesso.eurekaprometheusbridge

import com.google.common.io.CharStreams
import junit.framework.TestCase.assertEquals
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.junit.Test
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit4.SpringRunner
import java.io.InputStreamReader
import javax.annotation.Resource

@RunWith(SpringRunner::class)
@SpringBootTest
@DirtiesContext
class EurekaPrometheusBridgeApplicationTests {

    companion object {
        //With using a companion object the logger isnt created for each class instance, this is for best pratice purposes
        val log = LoggerFactory.getLogger(this::class.java.name)
    }

    @Resource
    lateinit var eurekaQuery: EurekaQuery

    @Resource
    lateinit var generator: Generator

    @Resource
    lateinit var scheduledJob: ScheduledJob

    @Test
    fun testConfigTemplates() {
        var eureka_config = EurekaProperties.configTemplate.reify()
        var prometheus_config = PrometheusProperties.configTemplate.reify()

        assertEquals(8761, eureka_config[EurekaProperties.port])
        assertEquals("http://127.0.0.1", eureka_config[EurekaProperties.host])
        assertEquals("/eureka/apps/", eureka_config[EurekaProperties.apiPath])
        assertEquals(false, eureka_config[EurekaProperties.showJson])

        assertEquals(15, prometheus_config.valueOf(PrometheusProperties.scrapeInterval))
        assertEquals(10, prometheus_config.valueOf(PrometheusProperties.scrapeTimeout))
        assertEquals("/eureka/apps/", prometheus_config.valueOf(PrometheusProperties.metricsPath))
        assertEquals("http", prometheus_config.valueOf(PrometheusProperties.scheme))
        assertEquals("/prometheus-basic.yml", prometheus_config.valueOf(PrometheusProperties.configFileTemplatePath))
        assertEquals("generated-prometheus-configs/prometheus.yml", prometheus_config.valueOf(PrometheusProperties.generatedConfigFilePath))
        assertEquals("/prometheus-test.yml", prometheus_config.valueOf(PrometheusProperties.testConfigFilePath))
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

        var resource = ClassPathResource(Generator.config[PrometheusProperties.testConfigFilePath]) //null if empty
        var resource2 = ClassPathResource(Generator.config[PrometheusProperties.generatedConfigFilePath]) //null if empty
        var template: String = "template1"
        var template2: String = "template2"

        Generator.config.get(PrometheusProperties.generatedConfigFilePath)
        /*if(!resource.exists() || !resource2.exists()){
            log.error(resource.toString())
            throw FileNotFoundException("The configFileTemplate wasn't found under: " + PrometheusProperties.configFileTemplatePath +
                    "\n The App can't start and will shut down.")        }
        else {*/
        var resourceInputStream = resource.inputStream
        var resourceInputStream2 = resource2.inputStream
        template = CharStreams.toString(InputStreamReader(resourceInputStream, "UTF-8"))
        template2 = CharStreams.toString(InputStreamReader(resourceInputStream2, "UTF-8"))


        assertEquals(template, template2)
    }

    @Test
    fun testEurekaQuery() {
        eurekaQuery.queryEureka()
    }

    @Test
    fun testScheduledJobs() {
        scheduledJob.executeBridge()
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
        val log = LoggerFactory.getLogger(ScheduledJob::class.java.name)
        val start = System.currentTimeMillis()
        log.info("Going to call the method: " + joinPoint.signature.name)
        joinPoint.proceed()
        log.info("Method execution completed: " + joinPoint.signature.name)
        val elapsedTime = System.currentTimeMillis() - start
        log.info("Method execution time: $elapsedTime milliseconds for: " + joinPoint.signature.name)
    }
}
