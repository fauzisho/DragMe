# Use OpenJDK 17
FROM openjdk:17-jdk-slim

# Install needed packages
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy shared module (needed for server)
COPY shared shared

# Copy server module
COPY server server

# Make gradlew executable
RUN chmod +x ./gradlew

# Just build the server classes - simplest approach
RUN ./gradlew :server:build --no-daemon -x test

# Expose port (Railway will set PORT env var)
EXPOSE $PORT

# Health check with longer timeout
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

# Set memory limits and run the server
ENV JAVA_OPTS="-Xmx256m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx256m"

# Run the server using Gradle but with memory limits
CMD ["sh", "-c", "export JAVA_OPTS=\"$JAVA_OPTS\" && ./gradlew :server:run --no-daemon"]
