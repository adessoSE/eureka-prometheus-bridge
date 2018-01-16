package de.adesso.eurekaprometheusbridge

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ScheduledJobs(
        @Autowired var eurekaQuery: EurekaQuery
        ){

    /**Queries Eureka for all App-Data*/
    @Scheduled(fixedRate = 10000)
    fun queryEureka() {
        eurekaQuery.queryEureka()
    }
}