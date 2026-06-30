#!/usr/bin/env bash
# Run province-service against the REAL shared DB over WireGuard (dev profile).
# Requires: the WireGuard tunnel up, and the gitignored creds file present.
# Override the creds path with POSTGRES_APP_ENV_FILE if it lives elsewhere.
set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE="${POSTGRES_APP_ENV_FILE:-$HOME/sauces/k8s/configs/postgres-cnpg/local-secret-postgres-app.env}"
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing creds file: $ENV_FILE" >&2
  echo "Generate it in the k8s project, or set POSTGRES_APP_ENV_FILE to its path." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

SPRING_PROFILES_ACTIVE=dev exec ./gradlew bootRun
