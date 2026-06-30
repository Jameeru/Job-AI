# ── Build stage ───────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy POM first — Docker layer caching: dependencies only re-downloaded when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Non-root user for security
RUN addgroup -S jobai && adduser -S jobai -G jobai
USER jobai

WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/job-ai-backend-*.jar app.jar

# Create directories for generated files
RUN mkdir -p /app/generated/resumes /app/generated/cover-letters

# Expose the default Spring Boot port
EXPOSE 8080

# Health check (Render uses this)
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1

# JVM tuning for containers (Render free tier: 512MB RAM)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseG1GC", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
