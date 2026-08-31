#!/usr/bin/env bash
#
# scripts/cleanup.sh
#
# Reclaims disk space from artifacts this project accumulates over time:
# dangling Docker images/build cache, old systemd release directories, and
# old backup archives beyond the retention count.
#
# Usage:
#   ./scripts/cleanup.sh [--dry-run] [--keep-releases N] [--keep-backups N]
#
# Defaults: --keep-releases 3, --keep-backups 5
#
# Exit codes:
#   0 success (even if there was nothing to clean)

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

DRY_RUN=false
KEEP_RELEASES=3
KEEP_BACKUPS=5
APP_HOME="${APP_HOME:-/opt/valorant-devops-app}"

while [ $# -gt 0 ]; do
  case "$1" in
    --dry-run)
      DRY_RUN=true
      shift
      ;;
    --keep-releases)
      KEEP_RELEASES="${2:-3}"
      shift 2
      ;;
    --keep-backups)
      KEEP_BACKUPS="${2:-5}"
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

run() {
  if [ "$DRY_RUN" = true ]; then
    echo "  [dry-run] $*"
  else
    eval "$@"
  fi
}

log_info "Cleanup starting (dry-run: $DRY_RUN)"

# ---- Docker: dangling images and build cache ----
if command -v docker >/dev/null 2>&1; then
  dangling=$(docker images -f "dangling=true" -q)
  if [ -n "$dangling" ]; then
    log_info "Removing dangling Docker images..."
    run "docker rmi $dangling"
  else
    log_info "No dangling Docker images to remove."
  fi

  log_info "Pruning stopped containers and unused build cache..."
  run "docker container prune -f"
  run "docker builder prune -f"
else
  log_warn "docker not found, skipping Docker cleanup."
fi

# ---- systemd releases: keep only the N most recent ----
if [ -d "$APP_HOME/releases" ]; then
  release_count=$(find "$APP_HOME/releases" -maxdepth 1 -mindepth 1 -type d | wc -l)
  if [ "$release_count" -gt "$KEEP_RELEASES" ]; then
    to_delete=$((release_count - KEEP_RELEASES))
    log_info "Found $release_count releases, keeping $KEEP_RELEASES, removing $to_delete oldest..."
    # shellcheck disable=SC2012
    ls -1dt "$APP_HOME"/releases/*/ | tail -n "$to_delete" | while read -r old_release; do
      run "rm -rf \"$old_release\""
      log_info "Removed old release: $old_release"
    done
  else
    log_info "$release_count release(s) present, within retention of $KEEP_RELEASES."
  fi
else
  log_info "No $APP_HOME/releases directory found (systemd deployment not in use here)."
fi

# ---- old backup archives ----
if [ -d "$SCRIPT_DIR/backups" ]; then
  backup_count=$(find "$SCRIPT_DIR/backups" -maxdepth 1 -name 'valorant-devops-app-backup-*.tar.gz' | wc -l)
  if [ "$backup_count" -gt "$KEEP_BACKUPS" ]; then
    to_delete=$((backup_count - KEEP_BACKUPS))
    log_info "Found $backup_count backups, keeping $KEEP_BACKUPS, removing $to_delete oldest..."
    # shellcheck disable=SC2012
    ls -1t "$SCRIPT_DIR"/backups/valorant-devops-app-backup-*.tar.gz | tail -n "$to_delete" | while read -r old; do
      run "rm -f \"$old\""
      log_info "Removed old backup: $(basename "$old")"
    done
  else
    log_info "$backup_count backup(s) present, within retention of $KEEP_BACKUPS."
  fi
fi

log_success "Cleanup finished."
