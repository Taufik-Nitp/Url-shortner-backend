FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/urlshortner-0.0.1-SNAPSHOT.jar /app/urlshortner.jar

ENTRYPOINT ["java", "-jar", "urlshortner.jar"]