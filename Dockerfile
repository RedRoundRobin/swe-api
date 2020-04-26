# docker run --run -d -p 9999:9999 rrr/api
FROM adoptopenjdk/maven-openjdk11:latest
RUN mkdir -p /usr/src/api/tmp
COPY start.sh /usr/src/api
COPY . /usr/src/api/tmp
EXPOSE 9999
WORKDIR /usr/src/api
CMD ["sh", "start.sh"]
