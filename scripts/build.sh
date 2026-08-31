#!/usr/bin/env bash
#
# scripts/build.sh
#
# Builds the application: runs the Maven lifecycle to produce the
# executable JAR, then (unless --jar-only is given) builds the Docker
# image for it.
#
# Usage:
#   ./scripts/build.sh                     # mvn package + docker build, tag "local"
#   ./scripts/build.sh --skip-tests        # mvn package -DskipTests + docker build
#   ./scripts/build.sh --tag 42            # docker build tagged valorant-devops-app:42
#   ./scripts/build.sh --jar-only          # only run Maven, skip the Docker build
#
# Exit codes:
#   0 success
#   1 Maven build/test failure
#   2 Docker build failure
#   3 invalid arguments

set -euo pipefail
SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
REPO_ROOT="$(cd -- "$SCRIPT_DIR/.." &>/dev/null && pwd)"
# shellcheck source=./lib.sh
source "$SCRIPT_DIR/lib.sh"

TAG="local"
SKIP_TESTS=false
JAR_ONLY=false

while [ $# -gt 0 ]; do
  case "$1" in
    --tag)
      TAG="${2:-}"
      [ -n "$TAG" ] || die "--tag requires a value" 3
      shift 2
      ;;
    --skip-tests)
      SKIP_TESTS=true
      shift
      ;;
    --jar-only)
      JAR_ONLY=true
      shift
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

require_cmd mvn
cd "$REPO_ROOT"

log_info "Building JAR with Maven (skip tests: $SKIP_TESTS)..."
if [ "$SKIP_TESTS" = true ]; then
  MAVEN_GOAL="clean package -DskipTests"
else
  MAVEN_GOAL="clean package"
fi

# shellcheck disable=SC2086
if ! mvn -B $MAVEN_GOAL; then
  die "Maven build failed - fix compilation/test errors before continuing." 1
fi
log_success "Maven build finished. JAR: target/valorant-devops-app.jar"

if [ "$JAR_ONLY" = true ]; then
  log_info "--jar-only set, skipping Docker build."
  exit 0
fi

require_cmd docker
log_info "Building Docker image valorant-devops-app:$TAG ..."
if ! docker build -t "valorant-devops-app:$TAG" .; then
  die "Docker build failed." 2
fi

log_success "Docker image built: valorant-devops-app:$TAG"
echo "$TAG" > "$SCRIPT_DIR/.last-build-tag"
log_info "Recorded last build tag in scripts/.last-build-tag for deploy.sh to pick up."
