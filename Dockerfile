# docker run --run -d -p 9999:9999 rrr/api
FROM adoptopenjdk/maven-openjdk11
COPY . /usr/src/api/temp
EXPOSE 9999
WORKDIR /usr/src/api/temp
CMD ["sh", "start.sh"]
