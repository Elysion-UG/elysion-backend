# --------------------------------------------------
# 1) Build-Stage: kompiliere das JAR
# --------------------------------------------------
FROM maven:3.8.7-jdk-17 AS build

WORKDIR /app

# Maven-Wrapper und POM
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Abhängigkeiten vorab herunterladen
RUN ./mvnw dependency:go-offline -B

# Source kopieren und builden
COPY src src
RUN ./mvnw package -DskipTests -B

# --------------------------------------------------
# 2) Runtime-Stage: schlanker JRE-Container
# --------------------------------------------------
FROM eclipse-temurin:17-jre-jammy

# Arbeitsverzeichnis
WORKDIR /app

# Das gebaute JAR aus der Build-Stage übernehmen
COPY --from=build /app/target/*-runner.jar user-service.jar

# Port freigeben (muss zum quarkus.http.port passen, default 8080)
EXPOSE 8080

# ENV für Deinen Pepper (oder über `docker run -e PEPPER=…`)
ENV PEPPER="pepperPig"

# Startkommando
ENTRYPOINT ["java","-jar","/app/user-service.jar"]
