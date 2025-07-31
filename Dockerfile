FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY target/quarkus/*-runner.jar user-service.jar

EXPOSE 8080
ENV PEPPER="pepperPig"
ENTRYPOINT ["java","-jar","/app/user-service.jar"]