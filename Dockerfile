# =========================================================================
# Multi-stage build for the VALORANT Tactical Hub.
#
# Stage 1 (build): compiles and packages the application with Maven inside
#                   a throwaway container - the build toolchain never ships
#                   in the final image.
# Stage 2 (runtime): a slim JRE-only image that just runs the JAR as a
#                    non-root user.
# =========================================================================

# ---------- Stage 1: build ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the POM first and resolve dependencies separately so Docker can cache
# this layer - it only gets re-run when pom.xml actually changes, not on
# every source code edit.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Now copy source and build. Tests are skipped here on purpose - they
# already ran (and must pass) as their own Jenkins/Maven stage before the
# Docker build stage is ever reached; re-running them inside the image
# build would just slow down every `docker build`.
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---------- Stage 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL org.opencontainers.image.title="valorant-devops-app" \
      org.opencontainers.image.description="VALORANT Tactical Hub - DevOps L1 practice application"

# curl is used by the HEALTHCHECK below and by anyone shelling into the
# container to manually probe /health during troubleshooting exercises.
RUN apk add --no-cache curl

# Run as a dedicated, unprivileged user rather than root.
RUN addgroup -S valorant && adduser -S -G valorant valorant

WORKDIR /app
COPY --from=build /build/target/valorant-devops-app.jar /app/app.jar
RUN chown -R valorant:valorant /app

USER valorant

# Sensible defaults - override at `docker run`/compose time.
ENV SERVER_PORT=8080 \
    APP_ENV=docker \
    BUILD_NUMBER=local \
    GIT_COMMIT=unknown

EXPOSE 8080

# Docker will mark the container unhealthy if /health stops responding,
# which docker-compose and `docker ps` both surface.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f "http://localhost:${SERVER_PORT}/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
