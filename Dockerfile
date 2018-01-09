## Testing Application ---------------------------------------------- "
# note: run gradlew build" before executing this

## Note this proved to be not making sense, because ports cant be dynamically opened while the container is running.

#FROM alpine:latest
#RUN apk --update add openjdk8
#CMD ["/usr/bin/java", "-version"]
#RUN echo "Java Version" java -version
#RUN mkdir src && cd src && mkdir generated-prometheus-configs
#ADD build/libs/eurekaprometheusbridge-0.0.1-SNAPSHOT.jar container/bridge-0.0.1.jar
#RUN java -jar container/bridge-0.0.1.jar
#Start Bridge when container starts
#ENTRYPOINT ["bash","-c","rm -f repo && java -jar container/bridge-0.0.1.jar"]
#EXPOSE 8080

#### Testing yml with prometheus -------------------------------------
FROM prom/prometheus:latest

COPY /generated-prometheus-config/prometheus.yml /etc/prometheus/prometheus.yml

VOLUME /prometheus-data
EXPOSE 9090:9090

