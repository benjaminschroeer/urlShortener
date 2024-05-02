FROM openjdk:17-jdk-alpine

MAINTAINER schroeer.b

EXPOSE 8080

COPY target/urlShortener-*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]