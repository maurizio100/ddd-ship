# ------------ Build Dependencies ------------
FROM maven:3.9-eclipse-temurin-21 AS deps
WORKDIR /app

COPY pom.xml .
COPY application/pom.xml application/pom.xml
COPY domain/pom.xml domain/pom.xml
COPY driven-adapter/pom.xml driven-adapter/pom.xml
COPY driving-adapter/pom.xml driving-adapter/pom.xml

RUN mvn -B -e -C dependency:go-offline


# ------------ Build Stage ------------
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /app /app

COPY application/src application/src
COPY domain/src domain/src
COPY driven-adapter/src driven-adapter/src
COPY driving-adapter/src driving-adapter/src

RUN mvn clean install -DskipTests


# ------------ Runtime Stage ------------
FROM eclipse-temurin:21-jre
WORKDIR /

COPY --from=builder /app/application/target/application-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
