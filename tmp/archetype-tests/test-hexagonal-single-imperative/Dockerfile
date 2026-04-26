# Multi-stage Dockerfile with jlink optimization
# Stage 1: Build application
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Remove archetype-generator plugin from build.gradle.kts (only needed for project generation)
RUN sed -i '/co\.com\.pragma\.archetype-generator/d' build.gradle.kts

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build application
RUN gradle bootJar --no-daemon

# Stage 2: Create custom JRE with jlink
FROM eclipse-temurin:21-jdk AS jlink

WORKDIR /jlink

# Copy the built JAR
COPY --from=builder /app/build/libs/*.jar app.jar

# Extract JAR to analyze dependencies
RUN jar -xf app.jar

# Create custom JRE with only required modules
RUN jlink \
    --add-modules java.base,java.logging,java.xml,java.naming,java.desktop,java.management,java.sql,java.instrument,java.security.jgss,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre

# Stage 3: Runtime
FROM debian:bookworm-slim

# Install required packages
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy custom JRE from jlink stage
COPY --from=jlink /jre /opt/jre

# Copy JAR from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# JVM optimization flags for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -XX:+OptimizeStringConcat \
    -Djava.security.egd=file:/dev/./urandom"

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application with custom JRE
ENTRYPOINT ["/opt/jre/bin/java"]
CMD ["-jar", "app.jar"]
