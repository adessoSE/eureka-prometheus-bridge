### Spins up a local Prometheus server and uses the generated configurationfile
FROM prom/prometheus:latest
ADD /generated-prometheus-configs/prometheus.yml /etc/prometheus/prometheus.yml
VOLUME /prometheus-data
EXPOSE 9090:9090

