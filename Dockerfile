# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY demo/pom.xml .
COPY demo/src ./src

# Download dependencies (this layer will be cached unless dependencies change)
RUN mvn dependency:go-offline -B

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user for security
RUN addgroup --system --gid 1001 appuser && \
    adduser --system --uid 1001 --ingroup appuser appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appuser /app

# Use the non-root user
USER appuser

# Configure JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

#environment variables for Supabase configuration
ARG SUPABASE_DB_URL
ARG SUPABASE_DB_USER
ARG SUPABASE_DB_PASSWORD
ARG SUPABASE_URL
ARG SUPABASE_KEY

ENV SUPABASE_DB_URL=$SUPABASE_DB_URL \
    SUPABASE_DB_USER=$SUPABASE_DB_USER \
    SUPABASE_DB_PASSWORD=$SUPABASE_DB_PASSWORD \
    SUPABASE_URL=$SUPABASE_URL \
    SUPABASE_KEY=$SUPABASE_KEY

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
