#!/bin/sh

cd /opt/driver-did-factom/
mvn --settings settings.xml jetty:run -P war
