FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY target/fithub-0.0.1-SNAPSHOT.jar fithub.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "fithub.jar"]
