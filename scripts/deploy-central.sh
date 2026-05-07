#!/bin/zsh
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/.." && pwd)"
main_pom="$repo_root/pom.xml"
central_pom="$repo_root/pom-central.xml"
backup_pom="$repo_root/pom.xml.release-backup"

if [[ ! -f "$central_pom" ]]; then
  echo "Missing $central_pom"
  exit 1
fi

restore_pom() {
  if [[ -f "$backup_pom" ]]; then
    mv "$backup_pom" "$main_pom"
  fi
}

trap restore_pom EXIT INT TERM

cp "$main_pom" "$backup_pom"
cp "$central_pom" "$main_pom"

cd "$repo_root"
mvn -f pom.xml -Pcentral-release clean deploy "$@"