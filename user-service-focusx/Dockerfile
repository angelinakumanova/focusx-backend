FROM maven:openjdk AS build
COPY . .
RUN mvn clean package -DskipTests

# Base image with Java and Maven
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Run the app
CMD ["java", "-jar", "target/focusx-0.0.1-SNAPSHOT.jar"]