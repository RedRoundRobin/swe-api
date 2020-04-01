# docker run --run -d -p 9999:9999 rrr/api
FROM adoptopenjdk/openjdk11:x86_64-alpine-jre-11.0.6_10
COPY . /usr/src/api/temp
EXPOSE 9999
WORKDIR /usr/src/api/temp
CMD ["sh", "start.sh"]
