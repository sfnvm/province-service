# syntax=docker/dockerfile:1

### Build stage — compile and produce the Spring Boot layered jar ###
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Cache dependencies: copy build scripts + wrapper first, resolve, then copy sources.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew --no-daemon dependencies >/dev/null 2>&1 || true

COPY src ./src
# Commit sha for /actuator/info (the build context has no .git). CI passes GITHUB_SHA.
ARG GIT_COMMIT=unknown
# bootJar only (does not run the plain `jar` task or tests); CI runs tests separately.
RUN ./gradlew --no-daemon clean bootJar -PgitCommit="$GIT_COMMIT"
# Split into cache-friendly pieces. The `tools` jarmode emits a thin runnable jar
# (Class-Path → lib/) under application/ plus the dependency lib/ under dependencies/.
# Rename the app jar to a stable name so the runtime entrypoint is version-agnostic.
RUN java -Djarmode=tools -jar build/libs/*.jar extract --layers --destination /workspace/extracted \
    && mv /workspace/extracted/application/*.jar /workspace/extracted/application/app.jar

### Runtime stage — minimal JRE, non-root, layered copy ###
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --home /app app

# Order matters for image-layer caching: least- to most-frequently changed.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app
EXPOSE 8080
# Let the container's memory limit drive the heap (k8s requests/limits set it).
# app.jar is the thin boot jar; its Class-Path resolves ./lib relative to /app.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
