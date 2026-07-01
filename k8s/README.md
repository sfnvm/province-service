# Deploying province-service

Manifests for the self-hosted Contabo cluster. The service joins the Istio ambient
mesh, is scraped by the existing Prometheus, and is exposed **internally** via APISIX
over WireGuard. Image is built by CI → Harbor (`.github/workflows/deploy.yml`).

## Deployment model — what auto-applies vs manual
- **Image/code changes** (`src/**`, `Dockerfile`, `build.gradle.kts`, …): pushing to
  `main` triggers CI, which builds, pushes to Harbor, and `kubectl set image` to roll
  the new image automatically.
- **Manifest changes in `k8s/`** (Deployment spec, Service, HPA, NetworkPolicies,
  ServiceMonitor, ApisixRoute, SealedSecrets, namespace): applied **manually** —
  `kubectl apply -f k8s/...`. CI does **not** trigger on `k8s/**` and never runs
  `kubectl apply`; there is no Argo/Flux GitOps controller. Committing a manifest just
  versions it; run `kubectl apply` to deploy the change.

## Prerequisites
- WireGuard up (private Harbor / API server), `kubectl`, `kubeseal` (v0.38.1).
- A Harbor **robot account** (`HARBOR_USERNAME` / `HARBOR_PASSWORD`) — not admin.
- Image pushed to `harbor.sfnvm.com/library/province-service` (CI or `docker build/push`).

### CI secrets (GitHub Actions → `.github/workflows/deploy.yml`)
The workflow runs on a GitHub-hosted runner that connects over WireGuard, then
builds/pushes and rolls the Deployment. Required repo secrets:
- `HARBOR_USERNAME` / `HARBOR_PASSWORD` — Harbor robot account.
- `WG_CONFIG` — WireGuard client conf (the `github-actions` peer).
- `KUBE_CONFIG_B64` — base64-encoded kubeconfig (API reachable over WG).

The Deploy step uses `kubectl set image`, so **the first roll-out (steps 1–2 below)
is manual**; CI updates the image on subsequent pushes.

## Database — nothing to provision
Prod uses the same CNPG DB dev uses: `postgres-rw.postgres.svc.cluster.local:5432/vietnamese_provinces`,
already populated, Flyway-baselined at v1, owned by `app` (see `../scripts/db-grants.sql`).
Flyway's `V1` is skipped on the baselined DB — only the creds secret is needed.

## 1. Generate the sealed secrets (once)
```bash
export HARBOR_USERNAME='robot$province' HARBOR_PASSWORD='...'
./k8s/generate-secrets.sh          # writes sealedsecret-postgres.yaml + sealedsecret-harbor-auth.yaml
```
These are safe to commit (encrypted). They create `province-postgres` (keys
`POSTGRES_USER`/`POSTGRES_PASSWORD`) and `harbor-auth` in the `province` namespace.

## 2. Apply, in order
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/sealedsecret-postgres.yaml -f k8s/sealedsecret-harbor-auth.yaml
kubectl apply -f k8s/deployment.yaml -f k8s/service.yaml
kubectl -n province rollout status deploy/province-service

kubectl apply -f k8s/servicemonitor.yaml       # metrics
kubectl apply -f k8s/apisixroute.yaml           # internal gateway route
kubectl apply -f k8s/hpa.yaml                   # autoscaling
# NetworkPolicies LAST — verify the app still works after applying:
kubectl apply -f k8s/networkpolicies.yaml
```

## 3. Verify
```bash
kubectl -n province get pods,svc,hpa
kubectl -n province logs deploy/province-service | grep -i flyway   # "Current version ... : 1" (V1 skipped)
# Over WireGuard, via the central gateway (service namespaced under /province-service):
curl 'http://10.8.0.1:9080/province-service/api/v1/provinces?size=3'
curl 'http://10.8.0.1:9080/province-service/api/v1/provinces/01'
# Metrics target up in Prometheus/Grafana:  up{namespace="province"}
istioctl ztunnel-config workloads -n province                        # meshed
```

## Notes
- The Deployment sets **no** `SPRING_PROFILES_ACTIVE` — the default `application.yaml`
  is the in-cluster config (DNS, OTLP→Alloy, ECS logs).
- `allow-metrics-from-monitoring` NetworkPolicy is required for Prometheus scraping
  under default-deny.
- Harbor host/port: the Deployment image mirrors the working `mdfk` ref
  (`harbor.sfnvm.com/...`). If CI push needs `:9443`, adjust the workflow/login accordingly.
