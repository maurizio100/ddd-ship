FROM eclipse-temurin:17-jdk-jammy as builder

LABEL authors="maurizio"
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY application/. application/.
COPY domain/. domain/.
COPY driven-adapter/. driven-adapter/.
COPY driving-adapter/. driving-adapter/.

RUN ./mvnw clean install

FROM eclipse-temurin:17-jre-jammy
EXPOSE 8080

COPY --from=builder /app/application/target/application-1.0.0-SNAPSHOT.jar /application.jar
ENTRYPOINT [ "java", "-jar", "application.jar"]