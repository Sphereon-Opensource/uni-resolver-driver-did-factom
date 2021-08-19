#!/bin/sh

cd /opt/driver-did-factom/
export
mvn jetty:run -P war
