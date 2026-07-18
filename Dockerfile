# ==========================================
# STAGE 1: Extract Layers
# ==========================================
FROM registry.company.com/gitops/templates:gradle-8-jdk-21 AS extractor
WORKDIR /app
COPY build/libs/*.jar app.jar

# Extract layers. This allows Docker to cache dependencies separately from app code.
RUN java -Djarmode=layertools -jar app.jar extract

# ==========================================
# STAGE 2: Secure Production Runtime
# ==========================================
FROM registry.company.com/gitops/templates:gradle-8-jre-21
WORKDIR /app

# Install wget for healthchecks since Alpine is minimal
RUN apk add --no-cache curl wget

# Copy extracted layers with correct ownership.
# Layer ordering is crucial: dependencies change least often, application code most often.
COPY --from=extractor --chown=1001:1001 /app/dependencies/ ./
COPY --from=extractor --chown=1001:1001 /app/spring-boot-loader/ ./
COPY --from=extractor --chown=1001:1001 /app/snapshot-dependencies/ ./
COPY --from=extractor --chown=1001:1001 /app/application/ ./

USER 1001

# Healthcheck: Crucial for Kubernetes 'readiness' and 'liveness' probes.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Duser.timezone=UTC", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75", \
            "-XX:+UseG1GC", \
            "org.springframework.boot.loader.launch.JarLauncher"]
