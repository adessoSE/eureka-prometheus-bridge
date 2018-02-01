FROM java:8-jre-alpine
ADD ./build/libs/eurekaprometheusbridge-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker","-jar","/app.jar"]
EXPOSE 1001