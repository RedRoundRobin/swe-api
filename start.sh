#!/bin/sh
cd apirest

mvn clean package

mv ./targer/apirest-*.jar ../

cd ../..

rm -rf temp 

java -jar /usr/src/api/apirest-*.jar --server.port=9999

echo "API started..."
