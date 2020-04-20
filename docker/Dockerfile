# Dockerfile for universalresolver/driver-did-factom

FROM universalresolver/base-ubuntu
MAINTAINER Sphereon <dev@sphereon.com>

ARG GITHUB_READ_PACKAGES_OWNER
ARG GITHUB_READ_PACKAGES_TOKEN

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

# done

ADD ./settings.xml /root/.m2/
ADD . /opt/driver-did-factom
RUN cd /opt/driver-did-factom && mvn --settings settings.xml clean install package -N -DskipTests

EXPOSE 8080

RUN chmod 555 /opt/driver-did-factom/docker/run-driver-did-factom.sh
CMD "/opt/driver-did-factom/docker/run-driver-did-factom.sh"
