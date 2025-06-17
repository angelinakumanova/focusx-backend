# 1️⃣ Base image with Java
FROM eclipse-temurin:21-jdk

# 2️⃣ Set working directory inside the container
WORKDIR /app

COPY target/focusx-0.0.1-SNAPSHOT.jar app.jar

# Run the JAR
CMD ["java", "-jar", "app.jar"]