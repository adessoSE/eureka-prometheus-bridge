FROM openjdk:8-jre-alpine
#RUN echo "Europe/Berlin" > /etc/timezone && dpkg-reconfigure -f noninteractive tzdata
ADD ./build/libs/eurekaprometheusbridge-0.0.1.jar myapp.jar

ADD generated-prometheus-configs/prometheus.yml generated-prometheus-configs/prometheus.yml:

ENV springprofiles="docker" MAXRAMIFNOLIMIT=4096

ENTRYPOINT MAXRAM=$(expr `cat /sys/fs/cgroup/memory/memory.limit_in_bytes` / 1024 / 1024) && \
           MAXRAM=$(($MAXRAM>$MAXRAMIFNOLIMIT?$MAXRAMIFNOLIMIT:$MAXRAM))m && \
           echo "MaxRam: $MAXRAM" && \
           java -XX:MaxRAM=$MAXRAM -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active="$springprofiles" myapp.jar

#when "-XX:+UseCGroupMemoryLimitForHeap" isn't experimental anymore, you can use the following
#ENTRYPOINT java -XX:+UseCGroupMemoryLimitForHeap -Djava.security.egd=file:/dev/./urandom -jar -Dspring.profiles.active="$springprofiles" myapp.jar

EXPOSE 1111