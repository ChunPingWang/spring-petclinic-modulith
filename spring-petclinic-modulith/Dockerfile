FROM eclipse-temurin:21-jre-alpine

ARG ARTIFACT_NAME
ARG EXPOSED_PORT

WORKDIR /app

# Copy the application JAR
COPY ${ARTIFACT_NAME}.jar /app/app.jar

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Expose the application port
EXPOSE ${EXPOSED_PORT}

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${EXPOSED_PORT}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
