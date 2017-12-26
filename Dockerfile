FROM alpine:latest

# Install OpenJdk 8
RUN apk --update add openjdk8
CMD ["/usr/bin/java", "-version"]
RUN echo "Java Version" java -version

ADD build/libs/eurekapromettheusbridge-0.0.1.jar container/bridge-0.0.1.jar
#RUN java -jar container/bridge-0.0.1.jar
#Start Bridge when container starts
#ENTRYPOINT ["bash","-c","rm -f repo && java -jar container/bridge-0.0.1.jar"]

EXPOSE 8080