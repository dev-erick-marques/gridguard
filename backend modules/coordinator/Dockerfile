FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/coordinator.jar coordinator.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "coordinator.jar"]