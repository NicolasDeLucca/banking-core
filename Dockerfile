# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Alpine's base image has no non-root user by default - the JVM would
# otherwise run as root inside the container, which turns any JVM
# container-escape/arbitrary-file-write vulnerability into a root compromise
# of the container instead of a restricted one.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /app/target/*.jar app.jar
USER app
EXPOSE 8080
# wget (BusyBox) is already on the Alpine base - no curl needed.
# /readiness (not the plain aggregate /health) so this only reports healthy
# once the app can actually reach Postgres, not just once the JVM booted -
# see the readiness group's "db" indicator in application.yml.
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD wget -q -O- http://localhost:8080/actuator/health/readiness | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
