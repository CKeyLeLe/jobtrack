#!/usr/bin/env bash
# End-to-end smoke test of the running Docker stack: proves that nginx, the API
# and PostgreSQL work together. Used by the CI pipeline, also runnable locally:
#   docker compose up -d --build && ./scripts/smoke-test.sh
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8000}"
EMAIL="smoke-$(date +%s)-$RANDOM@jobtrack.dev"
PASSWORD="smoke-test-password"

pass() { echo "PASS  $1"; }
fail() { echo "FAIL  $1" >&2; exit 1; }

# 1. The Angular app is served, deep links included, with security headers.
headers=$(curl -fsS -D - -o /dev/null "$BASE_URL/applications/1/edit") || fail "frontend not reachable at $BASE_URL"
grep -qi '^content-security-policy:' <<<"$headers" || fail "Content-Security-Policy header missing"
pass "frontend served on a deep link with a Content-Security-Policy"

# 2. The API is protected.
status=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/applications")
[ "$status" = "401" ] || fail "expected 401 without a token, got $status"
pass "API rejects anonymous requests (401)"

# 3. Registration returns a JWT.
token=$(curl -fsS -X POST "$BASE_URL/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
[ -n "$token" ] || fail "registration did not return a token"
pass "registration returns a JWT"

# 4. Data goes all the way to PostgreSQL and back.
curl -fsS -o /dev/null -X POST "$BASE_URL/api/applications" \
  -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
  -d '{"company":"Smoke Test Inc","jobTitle":"Java Developer","status":"APPLIED","appliedOn":"2026-01-01"}' \
  || fail "could not create an application"
curl -fsS "$BASE_URL/api/applications" -H "Authorization: Bearer $token" | grep -q 'Smoke Test Inc' \
  || fail "created application is not listed"
pass "application created and read back (nginx -> API -> PostgreSQL)"

echo "Smoke test passed."
