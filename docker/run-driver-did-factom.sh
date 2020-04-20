#!/bin/sh

cd /opt/driver-did-factom/
export
mvn --settings settings.xml jetty:run -P war
