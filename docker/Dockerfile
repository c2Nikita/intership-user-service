FROM eclipse-temurin:20-jdk
WORKDIR /app
RUN useradd -ms /bin/bash appuser
COPY build/libs/task-app-0.0.1.jar app.jar
USER appuser
ENTRYPOINT ["java","-Dspring.profiles.active=docker", "-jar", "app.jar"]