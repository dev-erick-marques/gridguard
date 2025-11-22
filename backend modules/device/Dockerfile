FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/device.jar device.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "device.jar"]
