[![GitHub issues](https://img.shields.io/github/issues/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/issues)
[![GitHub stars](https://img.shields.io/github/stars/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/stargazers)
[![GitHub license](https://img.shields.io/github/license/adessoAG/eureka-prometheus-bridge.svg?style=flat-square)](https://github.com/adessoAG/eureka-prometheus-bridge/blob/master/LICENSE)
![](https://img.shields.io/badge/Nice-100%25-brightgreen.svg)


# Bridge for Eureka and Prometheus.

### Features
The bridge pulls the service-urls from eureka. 
It provides a config-file for prometheus which contains the services /prometheus endpoint (or any endpoint you define), so prometheus knows where to scrape for metrics.

#### Test it
To test the project a fully dockerized spring-boot-kotlin app is available [here](https://github.com/adessoAG/eureka-prometheus-bridge-tester).

#### Configurable Parameters
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
```

### Start the application

1. In the project_dir use `gradlew build`
2. Run the jar under `.\build\libs\eureka-prometheus-bridge-0.0.1.jar`

### Start the Test-Microservice-Project

As you may notice, just running the app doesnt fetch any services. So i built a [spring boot project](https://github.com/silasmahler/eureka-prometheus-bridge-tester) with some example-services to use.

### Docker

##### Notice: 
Docker is only for testing purposes with the prometheus-server. Due to the possible change of ports during runtime in the app, which docker isnt supporting without creating a new container-instance, this was left out.

##### But:
If you start the Docker-Container with you will be able to test the prometheus-instance it is spinning up immediately. The generated configs will be used as configs for prometheus. Under your host on port 9090 you can find the server.

To start the container exceute `docker build -t bridge_prometheus_test_server . && docker run -p 9090:9090 bridge_prometheus_test_server` in the project_dir.

### Releases and Dependency

Have a look at [Bintray](https://bintray.com/silasmahler/eureka-prometheus-bridge/eureka-prometheus-bridge/0.0.1)
as new releases will be available there as a dependency for future projects.
