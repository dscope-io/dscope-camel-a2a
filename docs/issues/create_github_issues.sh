#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ISSUES_DIR="${ISSUES_DIR:-$SCRIPT_DIR}"
DRY_RUN="false"
REPO_ARG=""

usage() {
  cat <<'EOF'
Create GitHub issues from issue markdown files.

Usage:
  create_github_issues.sh [--dry-run] [--repo owner/name] [--issues-dir path]

Options:
  --dry-run            Print parsed fields and gh commands without creating issues
  --repo owner/name    Target repository (passed as --repo to gh)
  --issues-dir path    Directory containing issue-*.md files
  -h, --help           Show this help

Expected issue file header format:
  **Title**: `...`
  **Labels**: `a`, `b`, `c`
  **Milestone**: `...`
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run)
      DRY_RUN="true"
      shift
      ;;
    --repo)
      REPO_ARG="$2"
      shift 2
      ;;
    --issues-dir)
      ISSUES_DIR="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ ! -d "$ISSUES_DIR" ]]; then
  echo "Issues directory not found: $ISSUES_DIR" >&2
  exit 1
fi

if [[ "$DRY_RUN" != "true" ]] && ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required." >&2
  exit 1
fi

if [[ "$DRY_RUN" != "true" ]]; then
  if ! gh auth status >/dev/null 2>&1; then
    echo "GitHub CLI is not authenticated. Run: gh auth login" >&2
    exit 1
  fi
fi

shopt -s nullglob
files=("$ISSUES_DIR"/issue-*.md)
shopt -u nullglob

if [[ ${#files[@]} -eq 0 ]]; then
  echo "No issue files found in: $ISSUES_DIR" >&2
  exit 1
fi

for file in "${files[@]}"; do
  title="$(sed -n 's/^\*\*Title\*\*: `\(.*\)`[[:space:]]*$/\1/p' "$file" | head -n 1)"
  labels_raw="$(sed -n 's/^\*\*Labels\*\*: \(.*\)[[:space:]]*$/\1/p' "$file" | head -n 1)"
  milestone="$(sed -n 's/^\*\*Milestone\*\*: `\(.*\)`[[:space:]]*$/\1/p' "$file" | head -n 1)"

  if [[ -z "$title" ]]; then
    echo "Skipping $file: missing Title" >&2
    continue
  fi

  # Body starts after the Milestone line.
  body_tmp="$(mktemp)"
  awk '
    seen_milestone == 0 && /^\*\*Milestone\*\*:/ { seen_milestone = 1; next }
    seen_milestone == 1 { print }
  ' "$file" > "$body_tmp"

  # Normalize labels: "`a`, `b`" -> "a,b"
  labels="$(echo "$labels_raw" | sed 's/`//g' | tr -d ' ' | sed 's/,,*/,/g')"
  if [[ "$labels" == "," || -z "$labels" ]]; then
    labels=""
  fi

  cmd=(gh issue create)
  if [[ -n "$REPO_ARG" ]]; then
    cmd+=(--repo "$REPO_ARG")
  fi
  cmd+=(--title "$title" --body-file "$body_tmp")
  if [[ -n "$labels" ]]; then
    cmd+=(--label "$labels")
  fi
  if [[ -n "$milestone" ]]; then
    cmd+=(--milestone "$milestone")
  fi

  if [[ "$DRY_RUN" == "true" ]]; then
    echo "-----"
    echo "FILE: $file"
    echo "TITLE: $title"
    echo "LABELS: ${labels:-<none>}"
    echo "MILESTONE: ${milestone:-<none>}"
    echo "COMMAND: ${cmd[*]}"
  else
    echo "Creating issue from: $file"
    "${cmd[@]}"
  fi

  rm -f "$body_tmp"
done

echo "Done."
