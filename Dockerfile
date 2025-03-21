FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY --from=build /app/target/crud-0.0.1-SNAPSHOT.jar /app/crud.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/crud.jar"]