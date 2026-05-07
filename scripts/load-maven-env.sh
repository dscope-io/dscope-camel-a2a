#!/bin/zsh

load_maven_env() {
  emulate -L zsh
  set -euo pipefail

  local props_file="${1:-$HOME/.m2/maven-central.properties}"

  if [[ ! -f "$props_file" ]]; then
    echo "Credentials properties file not found: $props_file" >&2
    return 1
  fi

  read_prop() {
    local key="$1"
    awk -F= -v target="$key" '
      /^[[:space:]]*#/ {next}
      NF < 2 {next}
      {
        k=$1
        sub(/^[[:space:]]+/, "", k)
        sub(/[[:space:]]+$/, "", k)
        if (k == target) {
          val=substr($0, index($0, "=")+1)
          sub(/^[[:space:]]+/, "", val)
          sub(/[[:space:]]+$/, "", val)
          print val
          exit
        }
      }
    ' "$props_file"
  }

  local ossrh_username
  local ossrh_password
  local gpg_passphrase
  local gpg_keyname

  ossrh_username="$(read_prop ossrhUsername || true)"
  ossrh_password="$(read_prop ossrhPassword || true)"
  gpg_passphrase="$(read_prop mavenGpgPassphrase || true)"
  gpg_keyname="$(read_prop mavenGpgKeyname || true)"

  if [[ -z "$ossrh_username" || -z "$ossrh_password" ]]; then
    echo "Missing ossrhUsername/ossrhPassword in $props_file" >&2
    return 1
  fi

  typeset -gx OSSRH_USERNAME="$ossrh_username"
  typeset -gx OSSRH_PASSWORD="$ossrh_password"

  if [[ -n "$gpg_passphrase" ]]; then
    typeset -gx MAVEN_GPG_PASSPHRASE="$gpg_passphrase"
  fi

  if [[ -n "$gpg_keyname" ]]; then
    typeset -gx MAVEN_GPG_KEYNAME="$gpg_keyname"
  fi

  echo "Loaded Maven env vars from $props_file"
  echo "- OSSRH_USERNAME: set"
  echo "- OSSRH_PASSWORD: set"
  if [[ -n "${MAVEN_GPG_PASSPHRASE:-}" ]]; then
    echo "- MAVEN_GPG_PASSPHRASE: set"
  else
    echo "- MAVEN_GPG_PASSPHRASE: missing"
  fi
  if [[ -n "${MAVEN_GPG_KEYNAME:-}" ]]; then
    echo "- MAVEN_GPG_KEYNAME: set"
  fi
}

load_maven_env "$@"