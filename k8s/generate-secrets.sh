#!/usr/bin/env bash
# Generate the committable SealedSecrets for province-service.
#
# Requires (run while connected to WireGuard):
#   - kubectl access to the cluster (KUBECONFIG)
#   - kubeseal (matching the sealed-secrets controller, v0.38.1)
#   - read access to `postgres-app` (ns postgres) and `harbor-auth` (ns default)
#
# Outputs (safe to commit):
#   k8s/sealedsecret-postgres.yaml       (DB creds → ns province, secret province-postgres)
#   k8s/sealedsecret-harbor-auth.yaml    (image pull → ns province, secret harbor-auth)
set -euo pipefail

NS=province
DIR="$(cd "$(dirname "$0")" && pwd)"
KS="kubeseal --controller-namespace sealed-secrets --controller-name sealed-secrets --format yaml"

echo "==> DB creds: reseal postgres-app (ns postgres) for ns ${NS}"
PGUSER=$(kubectl -n postgres get secret postgres-app -o jsonpath='{.data.username}' | base64 -d)
PGPASS=$(kubectl -n postgres get secret postgres-app -o jsonpath='{.data.password}' | base64 -d)
kubectl create secret generic province-postgres \
  --namespace "$NS" \
  --from-literal=POSTGRES_USER="$PGUSER" \
  --from-literal=POSTGRES_PASSWORD="$PGPASS" \
  --dry-run=client -o yaml | $KS >"$DIR/sealedsecret-postgres.yaml"
echo "    wrote k8s/sealedsecret-postgres.yaml"

echo "==> Harbor image-pull secret: reseal default/harbor-auth for ns ${NS}"
kubectl create secret generic harbor-auth \
  --namespace "$NS" \
  --type=kubernetes.io/dockerconfigjson \
  --from-file=.dockerconfigjson=<(kubectl -n default get secret harbor-auth -o jsonpath='{.data.\.dockerconfigjson}' | base64 -d) \
  --dry-run=client -o yaml | $KS >"$DIR/sealedsecret-harbor-auth.yaml"
echo "    wrote k8s/sealedsecret-harbor-auth.yaml"

echo "Done. Review, then commit both files and 'kubectl apply -f' them."
