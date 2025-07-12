# Stage 1: Build the application using Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom and download dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline

# Copy source and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application with Java 21 JDK
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy jar file from the build stage
COPY --from=build /app/target/*.jar fitnuz.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "fitnuz.jar"]
