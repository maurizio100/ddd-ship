FROM maven:3.9-eclipse-temurin-17 as deps

LABEL authors="maurizio"
WORKDIR /app

COPY pom.xml .
COPY application/pom.xml application/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY driven-adapter/pom.xml driven-adapter/pom.xml
COPY driving-adapter/pom.xml driving-adapter/pom.xml

RUN mvn -B -e -C dependency:go-offline

FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /app /app

COPY application/src /app/application/src
COPY domain/src /app/domain/src
COPY driven-adapter/src /app/driven-adapter/src
COPY driving-adapter/src /app/driving-adapter/src

RUN mvn clean install

FROM eclipse-temurin:17-jre-jammy

COPY --from=builder /app/application/target/application-1.0.0-SNAPSHOT.jar /application.jar

EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "application.jar"]