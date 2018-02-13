[![GitHub issues](https://img.shields.io/github/issues/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/issues)
[![GitHub stars](https://img.shields.io/github/stars/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/stargazers)
[![GitHub license](https://img.shields.io/github/license/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/blob/master/LICENSE)
![](https://img.shields.io/badge/Nice-100%25-brightgreen.svg)

# Bridge for Eureka and Prometheus.

# 1. General Information

### 1.1 Features
The bridge pulls the service-urls from eureka. 
It provides a config-file for prometheus which contains the services /prometheus endpoint (or any endpoint you define), so prometheus knows where to scrape for metrics.

#### 1.2 Test it
To test the project a fully dockerized spring-boot-kotlin app is available [here](https://github.com/adessoAG/eureka-prometheus-bridge-tester). More information in the Chapter "Starting the Application"
 

#### 1.3 Configurable Parameters
```yml
spring.application.name=eureka-prometheus-bridge

server.port=1111

eureka.client.register-with-eureka=false

query.interval.second=60

bridge.eureka.port=8761
bridge.eureka.host=http://127.0.0.1
bridge.eureka.apipath=/eureka/apps/
bridge.eureka.showJson=false

bridge.prometheus.scrapeinterval=15 
bridge.prometheus.scrapetimeout=10 
bridge.prometheus.metricspath=/prometheus 
bridge.prometheus.scheme=http
bridge.prometheus.generatedConfigFilePath=generated-prometheus-configs/prometheus.yml
bridge.prometheus.configFileTemplatePath=src/main/resources/prometheus-basic.yml
bridge.prometheus.testConfigFilePath=src/test/resources/prometheus-test.yml
```

# 2 Starting the Application

### 2.1 Start the Standalone Bridge Application

1. In the project_dir use `gradlew build`
2. Run the jar under `.\build\libs\eureka-prometheus-bridge-0.0.1.jar`

### 2.2 Start the Test-Microservice-Project

As you may notice, just running the app doesnt fetch any services. So i built a [spring boot project](https://github.com/silasmahler/eureka-prometheus-bridge-tester) with some example-services to use.

### 2.3 Starting everything at once (Docker-Compose needs to be installed)

Run `docker-compose up` in the root-directory.

The Applications will be available:
| Service        |url           | What it does  |
| -------------   |:-------------:| -----:|
| Eureka Dasboard   |localhost:8761| Shows you the services of the Microservice landscape|
| Prometheus-Server| localhost:9090| Monitors the services given in the prometheus.yml config file |
| Test-Service 1 | localhost:1001| Is a test-service with a simple endpoint /test |
| Test-Service 2 | localhost:1002| Is a test-service with a simple endpoint /test | |


# 3 Releases and Dependency

Have a look at [Bintray](https://bintray.com/silasmahler/eureka-prometheus-bridge/eureka-prometheus-bridge/0.0.1)
as new releases will be available there as a dependency for future projects.
