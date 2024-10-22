FROM maven:3.8.5-openjdk-17 as builder

# Copy your application's source code
COPY . /.

# Build the JAR file
RUN mvn clean package -DskipTests

# Stage 2: Run the Spring Boot application
FROM openjdk:17-alpine

# Copy the built JAR from the builder stage
COPY --from=builder target/*.jar app.jar

# Expose the application port (e.g., 8080)
EXPOSE 8080

# Run the Spring Boot application with environment variables
CMD ["java", "-Dspring.profiles.active=${SPRING_ACTIVE_PROFILE}", "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", "-Dspring.mail.host=${SMTP_HOST}", "-Dspring.mail.port=${SMTP_PORT}", "-Dspring.mail.username=${SMTP_USERNAME}", "-Dspring.mail.password=${SMTP_PASSWORD}", "-jar", "app.jar"]