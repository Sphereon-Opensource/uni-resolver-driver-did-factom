# Dockerfile for universalresolver/driver-did-factom

FROM universalresolver/base-ubuntu
MAINTAINER Sphereon <dev@sphereon.com>

ARG GITHUB_READ_PACKAGES_OWNER
ARG GITHUB_READ_PACKAGES_TOKEN
ADD ./settings.xml /root/.m2/

# build driver-did-factom

ADD . /opt/driver-did-factom
RUN cd /opt/driver-did-factom && cat /opt/driver-did-factom/docker/run-driver-did-factom.sh && mvn --settings settings.xml clean install package -N -DskipTests

# variables
ENV factomd_url=https://dev.factomd.net/v2
# ENV uniresolver_driver_did_work_domain=https://credentials.id.workday.com
# done

EXPOSE 8080

RUN chmod 555 /opt/driver-did-factom/docker/run-driver-did-factom.sh
CMD "/opt/driver-did-factom/docker/run-driver-did-factom.sh"
