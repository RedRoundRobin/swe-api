# docker run --run -d -p 9999:9999 rrr/api
FROM adoptopenjdk/maven-openjdk11:latest
COPY . /usr/src/api/tmp
EXPOSE 9999
WORKDIR /usr/src/api/tmp
CMD ["sh", "start.sh"]