#!/usr/bin/env bash
#
# scripts/deploy.sh
#
# Deploys the application using one of two supported paths:
#
#   --mode docker   (default) - runs the app + Nginx via docker compose,
#                     switching to a new image tag.
#   --mode systemd  - copies the JAR into a new timestamped release
#                     directory and restarts the systemd service.
#
# In both modes, the previously-active deployment is recorded so that
# scripts/rollback.sh can put it back if something goes wrong.
#
# Usage:
#   ./scripts/deploy.sh --mode docker [--tag <image-tag>]
#   ./scripts/deploy.sh --mode systemd [--jar <path-to-jar>]
#
# Environment overrides:
#   DEPLOY_STATE_DIR   where docker-mode current/previous tags are recorded
#                      (default: <repo>/.deploy-state)
#   APP_HOME           systemd-mode install root (default: /opt/valorant-devops-app)
#   HEALTH_URL         URL used for the post-deploy health check
#
# Exit codes:
#   0 success
#   1 deployment failed
#   2 health check failed after deploy (deployment happened, verify manually)
#   3 invalid arguments

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/.." &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

MODE="docker"
TAG=""
JAR_PATH="$REPO_ROOT/target/valorant-devops-app.jar"
DEPLOY_STATE_DIR="${DEPLOY_STATE_DIR:-$REPO_ROOT/.deploy-state}"
APP_HOME="${APP_HOME:-/opt/valorant-devops-app}"

while [ $# -gt 0 ]; do
  case "$1" in
    --mode)
      MODE="${2:-}"
      shift 2
      ;;
    --tag)
      TAG="${2:-}"
      shift 2
      ;;
    --jar)
      JAR_PATH="${2:-}"
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

case "$MODE" in
  docker) : ;;
  systemd) : ;;
  *) die "--mode must be 'docker' or 'systemd', got: $MODE" 3 ;;
esac

deploy_docker() {
  require_cmd docker
  mkdir -p "$DEPLOY_STATE_DIR"

  if [ -z "$TAG" ]; then
    if [ -f "$SCRIPT_DIR/.last-build-tag" ]; then
      TAG="$(cat "$SCRIPT_DIR/.last-build-tag")"
    else
      TAG="local"
    fi
  fi
  log_info "Deploying Docker image tag: $TAG"

  if ! docker image inspect "valorant-devops-app:$TAG" >/dev/null 2>&1; then
    die "Image valorant-devops-app:$TAG not found locally. Run scripts/build.sh --tag $TAG first." 1
  fi

  # Record whatever is currently deployed as "previous" before switching over.
  local current_tag_file="$DEPLOY_STATE_DIR/current_tag"
  local previous_tag_file="$DEPLOY_STATE_DIR/previous_tag"
  if [ -f "$current_tag_file" ]; then
    cp "$current_tag_file" "$previous_tag_file"
    log_info "Recorded previous deployment: $(cat "$previous_tag_file")"
  else
    log_warn "No previous deployment recorded yet (first deploy)."
  fi

  cd "$REPO_ROOT"
  export APP_IMAGE_TAG="$TAG"
  if ! docker compose up -d; then
    die "docker compose up failed." 1
  fi

  echo "$TAG" > "$current_tag_file"
  log_success "docker compose reports the stack is up with tag $TAG."
}

deploy_systemd() {
  [ -f "$JAR_PATH" ] || die "JAR not found at $JAR_PATH. Run scripts/build.sh --jar-only first." 1
  require_cmd systemctl

  local release_dir="$APP_HOME/releases/$(date +%Y%m%d%H%M%S)"
  log_info "Creating new release directory: $release_dir"
  mkdir -p "$release_dir"
  cp "$JAR_PATH" "$release_dir/valorant-devops-app.jar"

  # Preserve whatever "current" pointed at as "previous" before moving it.
  if [ -L "$APP_HOME/current" ]; then
    local existing_target
    existing_target="$(readlink -f "$APP_HOME/current")"
    ln -sfn "$existing_target" "$APP_HOME/previous"
    log_info "Recorded previous release: $existing_target"
  else
    log_warn "No previous release recorded yet (first deploy)."
  fi

  ln -sfn "$release_dir" "$APP_HOME/current"
  log_info "Restarting valorant-app.service..."
  sudo systemctl restart valorant-app
}

if [ "$MODE" = "docker" ]; then
  deploy_docker
else
  deploy_systemd
fi

HEALTH_URL="${HEALTH_URL:-http://localhost/health}"
log_info "Running post-deploy health check against $HEALTH_URL ..."
if "$SCRIPT_DIR/health-check.sh" "$HEALTH_URL" 10 3; then
  log_success "Deployment healthy."
  exit 0
else
  log_error "Deployment completed but health check failed. Consider: ./scripts/rollback.sh --mode $MODE"
  exit 2
fi
