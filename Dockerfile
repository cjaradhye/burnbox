# Multi-stage build optimized for Render
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

# Copy Maven configuration files
COPY pom.xml ./
COPY .mvn/ ./.mvn/
COPY mvnw ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Production stage - optimized for Render
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks and tzdata for timezone support
RUN apk add --no-cache curl tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone
ENV TZ=UTC

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create app directory and logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appgroup /app/app.jar

# Switch to non-root user
USER appuser

# Render uses PORT environment variable - expose it dynamically
EXPOSE ${PORT:-8080}

# Health check for Render - use PORT env var
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# JVM optimization for Render containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

# Run the application with PORT support for Render
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]