#!/bin/sh
cd apirest

mvn clean package

mv ./target/apirest-*.jar ../../apirest.jar
cd ../..
rm -rf tmp 

java -jar /usr/src/api/apirest.jar \
--server.port=9999 --spring.postgres.url=jdbc:postgresql://core.host.redroundrobin.site:6543/postgre \
--spring.timescale.url=jdbc:postgresql://core.host.redroundrobin.site:3456/timescale \
--telegram.url=http://core.host.redroundrobin.site:3000/

echo "API started..."
