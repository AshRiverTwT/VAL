#!/usr/bin/env bash
#
# Shared helper functions sourced by every script in this directory.
# Keeping logging/error-handling in one place means every script behaves
# consistently and none of them re-implement the same boilerplate.

# Colors (disabled automatically when not attached to a terminal).
if [ -t 1 ]; then
  readonly C_RESET="\033[0m" C_RED="\033[31m" C_YELLOW="\033[33m" C_GREEN="\033[32m" C_BLUE="\033[34m"
else
  readonly C_RESET="" C_RED="" C_YELLOW="" C_GREEN="" C_BLUE=""
fi

log_info() {
  echo -e "${C_BLUE}[INFO]${C_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $*"
}

log_warn() {
  echo -e "${C_YELLOW}[WARN]${C_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $*"
}

log_error() {
  echo -e "${C_RED}[ERROR]${C_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $*" >&2
}

log_success() {
  echo -e "${C_GREEN}[ OK ]${C_RESET} $(date '+%Y-%m-%d %H:%M:%S') - $*"
}

# die <message> [exit_code]
# Logs an error and exits. Every script uses this instead of scattering
# `exit 1` everywhere so failures are always logged with context.
die() {
  local message="$1"
  local code="${2:-1}"
  log_error "$message"
  exit "$code"
}

# require_cmd <command>
# Fails fast with a clear message if a required tool is missing, instead of
# letting the script fail later with a confusing "command not found".
require_cmd() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    die "Required command '$cmd' not found on PATH. Install it and try again." 127
  fi
}
