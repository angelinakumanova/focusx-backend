# ---------- STAGE 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy everything and build the JAR
COPY . .
RUN mvn clean package -DskipTests


# ---------- STAGE 2: Run ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy only the JAR from the build stage
COPY --from=build /app/target/focusx-0.0.1-SNAPSHOT.jar app.jar

# Run the JAR
CMD ["java", "-jar", "app.jar"]