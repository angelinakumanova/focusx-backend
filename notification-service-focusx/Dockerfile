# ---------- STAGE 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests


# ---------- STAGE 2: Run ----------
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/focusx-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]