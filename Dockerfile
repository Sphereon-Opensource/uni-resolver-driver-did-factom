# Dockerfile for universalresolver/driver-did-factom

FROM adoptopenjdk/openjdk11:jre
MAINTAINER Sphereon <dev@sphereon.com>

# Default mainnet node using Factom OpenNode
ENV NODE1_ENABLED true
ENV NODE1_NETWORK_ID mainnet
ENV NODE1_FACTOMD_URL https://api.factomd.net/v2

# Default testnet node using Factom OpenNode
ENV NODE2_ENABLED true
ENV NODE2_NETWORK_ID testnet
ENV NODE2_FACTOMD_URL https://dev.factomd.net/v2
#
# Additional nodes can be passed in using environment variables


EXPOSE 8080

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
