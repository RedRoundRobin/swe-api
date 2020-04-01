#!/bin/sh
cd apirest

apk update
apk add maven
mvn clean package

mv ./target/apirest-*.jar ../../apirest.jar
cd ../..
rm -rf tmp 

java -jar /usr/src/api/apirest.jar --server.port=9999

echo "API started..."
