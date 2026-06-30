#!/usr/bin/env bash
# Run province-service locally against a throwaway Postgres (compose.yaml).
# No credentials or VPN needed.
set -euo pipefail
cd "$(dirname "$0")/.."
SPRING_PROFILES_ACTIVE=local exec ./gradlew bootRun
