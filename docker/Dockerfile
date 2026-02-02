FROM openjdk:20-ea-1-jdk
WORKDIR /app
COPY build/libs/task-app-0.0.1.jar app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-jar", "app.jar"]