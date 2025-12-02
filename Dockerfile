# Use Eclipse Temurin JRE 21 as base image
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR file from target directory
COPY target/*.jar app.jar

# Expose port 8080 (application port)
EXPOSE 8080

# Set the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
