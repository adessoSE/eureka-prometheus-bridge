Bridge for Eureka and Prometheus.

The bridge scrapes information from eureka about all clients and pushes it in the pushgateway of prometheus. From there on the prometheusserver can take the information.

To run the bridge run the Dockerfile. 
Note: Its important, that a eureka-server instance is running. The port can be configured in the properties.
