# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A read-only REST API (Spring Boot 4.1, Kotlin, Java 21) exposing Vietnam's administrative
data — provinces, wards, administrative units/regions — from a shared Postgres database
(`vietnamese_provinces`). It runs on a self-hosted 3-node k8s cluster behind an internal
APISIX gateway. The repo holds the app plus its Dockerfile, CI, and k8s manifests.

## Commands

```bash
./gradlew build                 # compile + test + assemble
./gradlew test                  # runs a real Postgres via Testcontainers — Docker MUST be running
./gradlew test --tests "*ApiIntegrationTest*"          # single test class
./gradlew test --tests "com.sfnvm.province.web.ApiIntegrationTest.<method>"   # single method
./gradlew spotlessApply         # format (ktfmt 0.64 googleStyle, 2-space); spotlessCheck to verify
scripts/run-local.sh            # run with the `local` profile (throwaway compose Postgres, no VPN)
scripts/run-dev.sh              # run with the `dev` profile (real shared DB over WireGuard)
```

`bootRun` with no profile uses the **default** (in-cluster) config and will not start on a dev
machine — always pick `local` or `dev`.

## Spring profiles (the central concept)

The same app runs in four configurations selected by `SPRING_PROFILES_ACTIVE`. Understanding
these is essential before touching `application*.yaml`:

- **default** (`application.yaml`, no profile) — production, in-cluster. Datasource is
  `postgres-rw.postgres.svc.cluster.local`, creds from `${POSTGRES_USER}`/`${POSTGRES_PASSWORD}`,
  traces exported to Alloy OTLP, logs as ECS JSON. **This is what the container image runs** (the
  Deployment sets no `SPRING_PROFILES_ACTIVE`).
- **local** — compose Postgres (`compose.yaml`) auto-started by `spring-boot-docker-compose`;
  plain logs; tracing off.
- **dev** — the *real* shared cluster DB reached over WireGuard; `baseline-on-migrate`; plain
  logs; tracing off. Creds are sourced from a gitignored env file by `scripts/run-dev.sh`.
- **test** — Testcontainers Postgres 17 seeded from `src/test/resources/db/test-init.sql`;
  Flyway and Hibernate DDL disabled. All integration tests extend `AbstractPostgresIT`.

## Architecture

Layered under `com.sfnvm.province`:
`domain` (JPA entities) → `repository` (Spring Data, `@EntityGraph`, lowercased-`LIKE` search) →
`service` → `web` (controllers under `/api/v1`). Entities never leave the service layer: `web/dto`
+ `web/mapper` convert to DTOs, and `web/error` renders `ProblemDetail` 404s via a
`@RestControllerAdvice`. The DB is authoritative and schema is owned by Flyway
(`ddl-auto: validate`); this service only reads.

`config/` holds cross-cutting beans worth knowing about:
- `WebConfig` — `@EnableSpringDataWebSupport(VIA_DTO)` for stable `Page` JSON. Because that
  annotation bypasses `spring.data.web.pageable.*`, the max page size is set directly on the
  resolver bean (currently 200) — change it there, not in yaml.
- `RequestLoggingFilter` — one structured access-log line per API call (skips `/actuator`).
- `VersionInfoContributor` — feeds `/actuator/info` (see build-info note below).

## Non-obvious things that will bite you

- **Spring Boot 4 renames.** Starters are `spring-boot-starter-webmvc` (not `-web`) and
  `spring-boot-starter-flyway`. Jackson 3 lives under `tools.jackson.*`
  (`tools.jackson.module:jackson-module-kotlin`). Test slices use per-slice starters
  (`spring-boot-starter-{webmvc,data-jpa,flyway,validation}-test`) with relocated annotation FQNs.
- **Flyway `V1__init` is skipped on the shared DB.** `V1` packages the full schema + seed, but the
  cluster DB was populated by hand and baselined at version 1 (`baseline-on-migrate`), so V1 never
  runs there. New migrations must start at **V2**.
- **The image runs a flattened thin jar, not the boot launcher.** The Dockerfile uses
  `-Djarmode=tools ... extract --layers` and runs `java -jar app.jar` (entrypoint is *not*
  `JarLauncher`). Application classes and `META-INF/` sit at the jar root.
- **`/actuator/info` version/commit come from `VersionInfoContributor`, not Gradle build-info.**
  The Gradle `buildInfo` task did not get packaged into the layered thin jar, so version is read
  from the jar manifest and commit from a `GIT_COMMIT` env baked into the image (CI passes the sha
  as `--build-arg GIT_COMMIT`).

## Deployment (`k8s/`)

Full apply order and verification are in `k8s/README.md`. Key rules:

- **Manifests are applied by hand** (`kubectl apply -f k8s/...`). CI only builds/pushes the image
  and does `kubectl set image` — it does **not** apply manifests, and there is no GitOps
  controller. Editing a manifest and committing does not deploy it.
- **Image/code changes deploy automatically** via CI on push to `main` (build → Harbor →
  `set image` by sha).
- **Harbor is `harbor.sfnvm.com:9443`** (443 is the public edge) and its `library` project
  silently drops the `latest` tag — always deploy by an immutable **sha** tag.
- **APISIX routes need `spec.ingressClassName: apisix`** (the class is not cluster-default). The
  gateway namespaces the service under `/province-service/...` and strips that prefix
  (`proxy-rewrite`); only `/api/v1/*` and `/actuator/info` are exposed externally.
- Default-deny NetworkPolicies require an explicit egress allow for every dependency (Postgres
  5432, Alloy OTLP 4318, etc.) or connections silently time out.

## Secrets

DB creds and the Harbor pull secret are committed as **SealedSecrets** (encrypted, scoped to the
`province` namespace); generate/rotate them with `k8s/generate-secrets.sh`. Real `*.env` files and
kubeconfigs are gitignored — never commit them.
