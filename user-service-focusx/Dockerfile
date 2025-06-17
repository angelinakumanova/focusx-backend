# Base image with Java and Maven
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy only the Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# Give execution permissions
RUN chmod +x mvnw

# Copy the rest of your service's code
COPY . .

# Build the jar
RUN ./mvnw clean package -DskipTests

# Run the app
CMD ["java", "-jar", "target/focusx-0.0.1-SNAPSHOT.jar"]