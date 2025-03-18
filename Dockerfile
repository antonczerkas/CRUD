FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/crud-0.0.1-SNAPSHOT.jar /app/crud.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/crud.jar"]