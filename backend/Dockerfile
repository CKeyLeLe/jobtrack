# syntax=docker/dockerfile:1

# ---- Stage 1: build the jar with the full JDK and Maven ----
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

# Dependencies first: this layer is cached and only rebuilt when pom.xml changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Tests are not run here: database tests need Docker (Testcontainers).
# They run in CI before the image is built.
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests \
    && java -Djarmode=tools -jar target/jobtrack-backend-*.jar extract --layers --launcher --destination target/extracted

# ---- Stage 2: minimal runtime image (JRE only, no build tools, no sources) ----
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S jobtrack && adduser -S jobtrack -G jobtrack
WORKDIR /app

# Spring Boot layers, from least to most frequently changed: a code change
# only rebuilds the small "application" layer, not the dependencies.
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./

# Never run as root inside the container.
USER jobtrack
EXPOSE 8080
# Use the container's memory limit instead of the host's.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
