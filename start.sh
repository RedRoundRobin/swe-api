#!/bin/sh

if [ ! -f /usr/src/api/apirest.jar ]; then

	cd tmp/apirest

	mvn clean package > logs_belle.txt

	mv ./target/apirest-*.jar ../../apirest.jar
	cd ../..
	rm -rf tmp

fi

java -jar /usr/src/api/apirest.jar \
--server.port=9999 \
--spring.postgres.url=jdbc:postgresql://db-postgre:5432/postgre \
--spring.timescale.url=jdbc:postgresql://db-timescale:5432/timescale \
--telegram.url=http://thirema-telegram-bot:3000/ \
--kafka.bootstrapAddress=kafka-core:29092

echo "API started..."
