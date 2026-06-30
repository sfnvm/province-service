# HANDOFF — province-service

Context handoff from the `~/sauces/k8s` infra session (github `sfnvm/k8s-playground`).
This service does not exist yet — this doc seeds a fresh Claude Code session to
build it so it fits the existing self-hosted Kubernetes platform.

## What this service is

`province-service` — a Spring Boot (Kotlin) REST API exposing the **provinces of a
country**. It runs as a microservice on the existing 3-node Contabo k8s cluster.
New microservices live in **their own repo** (this folder), built by GitHub
Actions → Harbor; only their k8s manifests get committed.

## Decided stack (confirmed with the user)

| Choice | Value |
|---|---|
| Framework | Spring Boot **4.1.0** |
| Language | **Kotlin**, Java **21** |
| Web | **Servlet** — Spring Web (MVC) + virtual threads (`spring.threads.virtual.enabled=true`) |
| Data | **Spring Data JPA** + PostgreSQL (JDBC) + Flyway migrations |
| Build | Gradle **Kotlin DSL** preferred (see generation caveat) |
| Coordinates | group `com.sfnvm`, artifact `province-service`, package `com.sfnvm.provinceservice` |

## Generating the project (start.spring.io)

Dependencies (Initializr IDs): `web,actuator,prometheus,distributed-tracing,data-jpa,postgresql,flyway,validation`

```bash
curl -sS https://start.spring.io/starter.zip \
  -d type=gradle-project-kotlin -d language=kotlin -d bootVersion=4.1.0.RELEASE -d javaVersion=21 \
  -d groupId=com.sfnvm -d artifactId=province-service -d name=province-service \
  -d packageName=com.sfnvm.provinceservice -d description='API exposing the provinces of a country' \
  -d dependencies=web,actuator,prometheus,distributed-tracing,data-jpa,postgresql,flyway,validation \
  -o province-service.zip && unzip -o province-service.zip
```

**Caveats hit on 2026-06-30:**
- start.spring.io's **Gradle-Kotlin generator was returning intermittent HTTP 500s**
  on heavier dep sets (`kotlinCoroutinesHelpDocumentCustomizer`/`gradleBuild` bean
  error). Retry, or fall back to `type=maven-project` (Maven path worked), or
  generate minimal (`-d dependencies=web,actuator`) and add the rest by hand.
- **Do NOT add `cloud-resilience4j`** — Spring Cloud has no Boot 4.1.0 release yet;
  it 500s the generator. For L7 resilience add the plain
  `io.github.resilience4j:resilience4j-spring-boot3` dependency manually instead
  (the ambient mesh is L4-only until a waypoint is deployed).

## Platform integration — concrete endpoints

Everything below is LIVE on the cluster (see `~/sauces/k8s/CLAUDE.md` →
"Microservices platform layer").

- **Database (CloudNativePG):** cluster `postgres`, db `app`, owner `app`.
  - Write service: `postgres-rw.postgres.svc.cluster.local:5432`
  - Read service: `postgres-ro.postgres.svc.cluster.local:5432`
  - Credentials: Secret **`postgres-app`** in ns `postgres` (keys `username`,
    `password`, and ready-made `uri` / `jdbc-uri`). Re-seal into this app's
    namespace with Sealed Secrets, or mirror it.
- **Tracing:** OTLP → `http://alloy.monitoring.svc.cluster.local:4318/v1/traces`
  (HTTP) — Alloy forwards to Tempo. Sampling 0.10.
- **Metrics:** Prometheus scrapes any `ServiceMonitor` cluster-wide → add one for
  `/actuator/prometheus`. (Do NOT stand up a second Prometheus.)
- **Logs:** JSON to stdout with `trace_id`/`span_id` → Alloy → Loki.
- **Mesh (Istio ambient):** label the app namespace
  `istio.io/dataplane-mode=ambient` for automatic mTLS east-west. App speaks
  **plain HTTP** to other services (no app-side TLS). L7 routing needs a waypoint
  (not deployed).
- **API gateway (APISIX) — INTERNAL ONLY:** expose the API via an `ApisixRoute`
  (or Gateway API). Reached over WireGuard at `http://10.8.0.1:9080`. **Never** a
  public nginx Ingress or Istio ingress gateway.
- **Registry:** Harbor `harbor.sfnvm.com` (private, via WireGuard). CI pushes
  images over the `github-actions` WireGuard peer.

## `src/main/resources/application.yml` essentials

```yaml
spring:
  application: { name: province-service }
  datasource:
    url: jdbc:postgresql://postgres-rw.postgres.svc.cluster.local:5432/app
    username: ${POSTGRES_USER}
    password: ${POSTGRES_PASSWORD}
  jpa:
    hibernate.ddl-auto: validate   # Flyway owns schema
  threads:
    virtual: { enabled: true }
management:
  endpoints.web.exposure.include: health,info,prometheus
  endpoint.health.probes.enabled: true
  metrics.tags.application: ${spring.application.name}
  tracing.sampling.probability: 0.10
  otlp.tracing.endpoint: http://alloy.monitoring.svc.cluster.local:4318/v1/traces
server:
  shutdown: graceful
logging:
  structured.format.console: ecs    # JSON to stdout w/ trace_id/span_id
```

## Kubernetes manifests to write (commit to THIS repo, e.g. `k8s/`)

- **Namespace** — instantiate from the guardrails template (auto-joins the mesh):
  `sed 's/<app-ns>/province/g' ~/sauces/k8s/configs/platform-guardrails/namespace-template.yaml | kubectl apply -f -`
  Then the NetworkPolicies: `networkpolicies.yaml` (default-deny already allows
  DNS, ztunnel HBONE `15008`, ingress from `ingress-apisix`, egress to `postgres:5432`).
- **Deployment** — image from Harbor; set resource **requests** (HPA + quota need
  them); JVM `-XX:MaxRAMPercentage=75`; probes:
  liveness `/actuator/health/liveness`, readiness `/actuator/health/readiness`.
- **Service** (ClusterIP, the app port).
- **ServiceMonitor** — scrape `/actuator/prometheus`.
- **ApisixRoute** — internal route, e.g. `paths: [/provinces/*]`; add `key-auth` /
  `limit-count` plugins for API management.
- **SealedSecret** — the DB creds (`POSTGRES_USER`/`POSTGRES_PASSWORD`) via
  `kubeseal --controller-namespace sealed-secrets --controller-name sealed-secrets`.
- **HPA** — from `~/sauces/k8s/configs/platform-guardrails/hpa-template.yaml`.

## Reference docs in the infra repo (`~/sauces/k8s`)

- `CLAUDE.md` → "Microservices platform layer" + "Hard rules"
- `configs/istio/commands.md`, `configs/apisix/commands.md`,
  `configs/sealed-secrets/commands.md`, `configs/platform-guardrails/commands.md`
- `configs/observability/commands.md` → "Spring Boot Defaults"
- `configs/wireguard/commands.md` → "APISIX access" (the 9080 listener)

## Status / next steps

1. Generate the project (retry start.spring.io or use Maven).
2. Add `resilience4j-spring-boot3` if L7 resilience is wanted.
3. Province domain: entity + repository + REST controller + Flyway migration
   (`V1__provinces.sql`) seeding the country's provinces.
4. `k8s/` manifests per above; wire CI (GitHub Actions over WireGuard → Harbor).
5. Onboard the namespace to the mesh; expose via internal ApisixRoute.

> Tip: this is a fresh project folder — run `claude` here and point it at this
> file. The infra platform decisions are also in the account-level memory under
> `~/.claude/projects/-Users-sfnvm-sauces-k8s/memory/` (e.g.
> `microservices-platform-istio-apisix.md`).
