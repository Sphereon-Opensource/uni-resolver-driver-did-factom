# Dockerfile for universalresolver/driver-did-factom

FROM universalresolver/base-ubuntu
MAINTAINER Sphereon <dev@sphereon.com>

ARG GITHUB_READ_PACKAGES_OWNER
ARG GITHUB_READ_PACKAGES_TOKEN


# Driver arguments
ARG TESTNET_FACTOMD_URL=https://dev.factomd.net/v2
ARG MAINNET_FACTOMD_URL=https://api.factomd.net/v2
ARG CUSTOM_FACTOMD_URL=https://dev.factomd.net/v2

# done

ADD ./settings.xml /root/.m2/
ADD . /opt/driver-did-factom
RUN cd /opt/driver-did-factom && echo "Factomd URL: $TESTNET_FACTOMD_URL" && mvn --settings settings.xml clean install package -N -DskipTests

EXPOSE 8080

RUN chmod 555 /opt/driver-did-factom/docker/run-driver-did-factom.sh
CMD "/opt/driver-did-factom/docker/run-driver-did-factom.sh"
