# Stage 1: Build the application using Maven
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

# Copy Maven config and download dependencies first (for better build caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create minimal runtime image
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built jar from build stage and rename it to app.jar for simplicity
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
