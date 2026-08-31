#!/usr/bin/env bash
#
# scripts/backup.sh
#
# Creates a timestamped tar.gz archive of the things worth keeping before a
# deployment: the built JAR, non-secret configuration, and recent logs.
# Old backups beyond the retention count are pruned automatically.
#
# Usage:
#   ./scripts/backup.sh [--keep N] [--output-dir DIR]
#
# Defaults: --keep 5, --output-dir <repo>/scripts/backups
#
# Exit codes:
#   0 success
#   1 nothing to back up / tar failure

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/.." &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

KEEP=5
OUTPUT_DIR="$SCRIPT_DIR/backups"

while [ $# -gt 0 ]; do
  case "$1" in
    --keep)
      KEEP="${2:-5}"
      shift 2
      ;;
    --output-dir)
      OUTPUT_DIR="${2:-}"
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

require_cmd tar
mkdir -p "$OUTPUT_DIR"

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
ARCHIVE_NAME="valorant-devops-app-backup-$TIMESTAMP.tar.gz"
STAGING_DIR="$(mktemp -d)"
trap 'rm -rf "$STAGING_DIR"' EXIT

log_info "Staging backup contents in $STAGING_DIR ..."

found_something=false

if [ -f "$REPO_ROOT/target/valorant-devops-app.jar" ]; then
  mkdir -p "$STAGING_DIR/jar"
  cp "$REPO_ROOT/target/valorant-devops-app.jar" "$STAGING_DIR/jar/"
  found_something=true
  log_info "Included: target/valorant-devops-app.jar"
fi

if [ -d "$REPO_ROOT/config" ]; then
  mkdir -p "$STAGING_DIR/config"
  # Only copy non-secret example/config files, never a real .env with secrets.
  find "$REPO_ROOT/config" -maxdepth 1 -type f ! -name '*.env' -exec cp {} "$STAGING_DIR/config/" \;
  found_something=true
  log_info "Included: config/ (excluding *.env files)"
fi

for log_dir in "$REPO_ROOT/logs" "$REPO_ROOT/docker/data/logs"; do
  if [ -d "$log_dir" ] && [ -n "$(ls -A "$log_dir" 2>/dev/null)" ]; then
    mkdir -p "$STAGING_DIR/logs/$(basename "$log_dir")"
    cp -r "$log_dir"/* "$STAGING_DIR/logs/$(basename "$log_dir")/"
    found_something=true
    log_info "Included: $(realpath --relative-to="$REPO_ROOT" "$log_dir")"
  fi
done

if [ "$found_something" = false ]; then
  log_warn "Nothing found to back up yet (no built JAR, config, or logs). Build/run the app first."
  exit 1
fi

tar -czf "$OUTPUT_DIR/$ARCHIVE_NAME" -C "$STAGING_DIR" .
log_success "Backup created: $OUTPUT_DIR/$ARCHIVE_NAME ($(du -h "$OUTPUT_DIR/$ARCHIVE_NAME" | cut -f1))"

# Retention: keep only the $KEEP most recent backups.
backup_count=$(find "$OUTPUT_DIR" -maxdepth 1 -name 'valorant-devops-app-backup-*.tar.gz' | wc -l)
if [ "$backup_count" -gt "$KEEP" ]; then
  to_delete=$((backup_count - KEEP))
  log_info "Retention: $backup_count backups exist, keeping $KEEP, pruning $to_delete oldest..."
  # shellcheck disable=SC2012
  ls -1t "$OUTPUT_DIR"/valorant-devops-app-backup-*.tar.gz | tail -n "$to_delete" | while read -r old; do
    rm -f "$old"
    log_info "Removed old backup: $(basename "$old")"
  done
fi
