#!/usr/bin/env bash
#
# scripts/rollback.sh
#
# Switches back to the previously-active deployment recorded by
# scripts/deploy.sh. This is NOT the same as restarting or rebuilding:
#   - restart:  same code, process is just stopped/started again
#   - rebuild:  produces a brand new artifact from current source
#   - redeploy: ships the SAME artifact you already built, again
#   - rollback: switches to the PREVIOUS, already-known-good artifact
#
# Usage:
#   ./scripts/rollback.sh --mode docker
#   ./scripts/rollback.sh --mode systemd
#
# Exit codes:
#   0 rollback succeeded and health check passed
#   1 no previous deployment recorded, or rollback failed
#   2 rollback happened but health check failed

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/.." &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

MODE="docker"
DEPLOY_STATE_DIR="${DEPLOY_STATE_DIR:-$REPO_ROOT/.deploy-state}"
APP_HOME="${APP_HOME:-/opt/valorant-devops-app}"

while [ $# -gt 0 ]; do
  case "$1" in
    --mode)
      MODE="${2:-}"
      shift 2
      ;;
    -h|--help)
      grep '^#' "$0" | sed 's/^#//'
      exit 0
      ;;
    *)
      die "Unknown argument: $1 (use --help for usage)" 3
      ;;
  esac
done

rollback_docker() {
  local current_tag_file="$DEPLOY_STATE_DIR/current_tag"
  local previous_tag_file="$DEPLOY_STATE_DIR/previous_tag"

  [ -f "$previous_tag_file" ] || die "No previous deployment recorded in $previous_tag_file - nothing to roll back to." 1

  local previous_tag current_tag
  previous_tag="$(cat "$previous_tag_file")"
  current_tag="$( [ -f "$current_tag_file" ] && cat "$current_tag_file" || echo "unknown")"

  log_warn "Rolling back from tag '$current_tag' to previous tag '$previous_tag'..."

  if ! docker image inspect "valorant-devops-app:$previous_tag" >/dev/null 2>&1; then
    die "Previous image valorant-devops-app:$previous_tag is no longer available locally." 1
  fi

  cd "$REPO_ROOT"
  export APP_IMAGE_TAG="$previous_tag"
  if ! docker compose up -d; then
    die "docker compose up failed during rollback." 1
  fi

  # Swap the two records so a second rollback would go back to $current_tag.
  echo "$previous_tag" > "$current_tag_file"
  echo "$current_tag" > "$previous_tag_file"
  log_success "Rolled back to tag $previous_tag."
}

rollback_systemd() {
  [ -L "$APP_HOME/previous" ] || die "No previous release symlink at $APP_HOME/previous - nothing to roll back to." 1
  require_cmd systemctl

  local previous_target current_target
  previous_target="$(readlink -f "$APP_HOME/previous")"
  current_target="$( [ -L "$APP_HOME/current" ] && readlink -f "$APP_HOME/current" || echo "")"

  log_warn "Rolling back from $current_target to $previous_target..."
  ln -sfn "$previous_target" "$APP_HOME/current"
  [ -n "$current_target" ] && ln -sfn "$current_target" "$APP_HOME/previous"

  sudo systemctl restart valorant-app
  log_success "Rolled back to release: $previous_target"
}

if [ "$MODE" = "docker" ]; then
  rollback_docker
elif [ "$MODE" = "systemd" ]; then
  rollback_systemd
else
  die "--mode must be 'docker' or 'systemd', got: $MODE" 3
fi

HEALTH_URL="${HEALTH_URL:-http://localhost/health}"
log_info "Verifying rollback with a health check against $HEALTH_URL ..."
if "$SCRIPT_DIR/health-check.sh" "$HEALTH_URL" 10 3; then
  log_success "Rollback verified healthy."
  exit 0
else
  log_error "Rollback completed but health check still failing - manual investigation required."
  exit 2
fi
