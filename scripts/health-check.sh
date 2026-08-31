#!/usr/bin/env bash
#
# scripts/health-check.sh
#
# Polls a health endpoint until it reports healthy or the retry budget is
# exhausted. Used standalone, from deploy.sh, and from the Jenkins
# pipeline's "Health Check" stage - all three need the exact same logic,
# so it lives in one place.
#
# Usage:
#   ./scripts/health-check.sh [url] [retries] [delay_seconds]
#
# Defaults: url=http://localhost/health retries=10 delay_seconds=3
#
# Exit codes:
#   0 healthy
#   1 unhealthy after all retries

set -uo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

URL="${1:-http://localhost/health}"
RETRIES="${2:-10}"
DELAY="${3:-3}"

require_cmd curl

log_info "Health-checking $URL (up to $RETRIES attempts, ${DELAY}s apart)..."

attempt=1
while [ "$attempt" -le "$RETRIES" ]; do
  # Capture both the HTTP status code and body in one request.
  http_status=$(curl -s -o /tmp/health-check-body.$$ -w '%{http_code}' --max-time 5 "$URL" || echo "000")
  body=$(cat /tmp/health-check-body.$$ 2>/dev/null || echo "")
  rm -f /tmp/health-check-body.$$

  if [ "$http_status" = "200" ] && echo "$body" | grep -q '"status":"UP"'; then
    log_success "Healthy after $attempt attempt(s). Response: $body"
    exit 0
  fi

  log_warn "Attempt $attempt/$RETRIES: HTTP $http_status, body: ${body:-<empty>}"
  attempt=$((attempt + 1))
  [ "$attempt" -le "$RETRIES" ] && sleep "$DELAY"
done

log_error "Application did not become healthy after $RETRIES attempts."
exit 1
