# 1️⃣ Base image with Java
FROM eclipse-temurin:21-jdk

# 2️⃣ Set working directory inside the container
WORKDIR /app

# 3️⃣ Copy the project files into the container
COPY . .

RUN ./mvnw package -DskipTests

# 5️⃣ Define the startup command
CMD ["java", "-jar", "target/focusx-0.0.1-SNAPSHOT.jar"]