# Use an official Maven image with Java 11 installed as a build stage
FROM maven:3.8.1-openjdk-11 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and any other files necessary for dependency resolution
COPY pom.xml ./

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Build the project
RUN mvn clean package

# Use an official Java runtime image as the final stage
FROM openjdk:11-jre-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on (adjust this as necessary)
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
