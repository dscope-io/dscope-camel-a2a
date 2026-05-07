#!/bin/zsh
set -euo pipefail

tmp_openpgp="/tmp/camel-a2a-preflight-key-openpgp.asc"
tmp_ubuntu="/tmp/camel-a2a-preflight-key-ubuntu.asc"

echo Checking OSSRH env vars...
[[ -n "${OSSRH_USERNAME:-}" ]] && [[ -n "${OSSRH_PASSWORD:-}" ]]

echo Checking GPG secret keys...
gpg --list-secret-keys --keyid-format LONG >/dev/null

echo Checking for non-expired GPG secret key...
if ! gpg --list-secret-keys --with-colons | awk -F: '$1=="sec"{if(index($2,"e")==0) ok=1} END{exit ok?0:1}'; then
  echo "No non-expired GPG secret key found. Please renew or create a new signing key."
  exit 1
fi

echo Detecting signing key...
signing_key="${MAVEN_GPG_KEYNAME:-}"
if [[ -z "$signing_key" ]]; then
  signing_key="$(gpg --list-secret-keys --with-colons | awk -F: '$1=="sec" && index($2,"e")==0 && index($12,"s")>0 {print $5; exit}')"
fi

if [[ -z "$signing_key" ]]; then
  echo "Unable to determine a signing-capable secret key. Set MAVEN_GPG_KEYNAME explicitly."
  exit 1
fi

signing_fpr="$(gpg --with-colons --fingerprint --list-secret-keys "$signing_key" | awk -F: '$1=="fpr"{print $10; exit}')"
if [[ -z "$signing_fpr" ]]; then
  echo "Unable to resolve fingerprint for signing key: $signing_key"
  exit 1
fi

echo "- Using signing key: $signing_key"
echo "- Signing fingerprint: $signing_fpr"

echo Checking public key availability for signing fingerprint...
openpgp_code="$(curl -sS -o "$tmp_openpgp" -w "%{http_code}" "https://keys.openpgp.org/vks/v1/by-fingerprint/$signing_fpr" || true)"
ubuntu_code="$(curl -sS -o "$tmp_ubuntu" -w "%{http_code}" "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x$signing_fpr" || true)"

if [[ "$openpgp_code" != "200" && "$ubuntu_code" != "200" ]]; then
  echo "Public key not discoverable for fingerprint $signing_fpr"
  echo "- keys.openpgp.org status: $openpgp_code"
  echo "- keyserver.ubuntu.com status: $ubuntu_code"
  echo "Publish your public key to supported keyservers before Central deploy."
  exit 1
fi

echo "- keys.openpgp.org status: $openpgp_code"
echo "- keyserver.ubuntu.com status: $ubuntu_code"

echo Checking Maven settings server id...
grep -q ossrh ~/.m2/settings.xml
grep -q central ~/.m2/settings.xml

echo Preflight checks passed.