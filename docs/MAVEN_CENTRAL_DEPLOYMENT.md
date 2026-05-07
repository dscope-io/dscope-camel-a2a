# Maven Central Deployment Guide

This repository includes a dedicated Maven Central deployment build file:

- `pom-central.xml`

Canonical package URL (PURL): `pkg:maven/io.dscope.camel/camel-a2a`

Use it to publish without changing the default project build (`pom.xml`).

## Prerequisites

- Sonatype Central Portal publishing token configured for `io.dscope.camel`
- `~/.m2/settings.xml` contains server credentials for `central` (and optionally `ossrh` for snapshot compatibility)
- GPG key available on the machine used for signing

Example `settings.xml` snippet:

```xml
<servers>
  <server>
    <id>central</id>
    <username>${env.OSSRH_USERNAME}</username>
    <password>${env.OSSRH_PASSWORD}</password>
  </server>
  <server>
    <id>ossrh</id>
    <username>${env.OSSRH_USERNAME}</username>
    <password>${env.OSSRH_PASSWORD}</password>
  </server>
</servers>
```

## Credential Locations

Use one of these approaches (recommended order):

1. Environment variables (recommended for Maven)
   - `OSSRH_USERNAME`
   - `OSSRH_PASSWORD`
   - `MAVEN_GPG_PASSPHRASE`
   - Note: variable names keep `OSSRH_*` for compatibility in this repo, but values should be your Central token username/password.

2. Maven user settings
   - `~/.m2/settings.xml`
   - Keep `central`/`ossrh` server credentials as `${env.OSSRH_USERNAME}` and `${env.OSSRH_PASSWORD}` (do not hardcode secrets).

3. Dedicated Maven credentials properties file
   - Source file: `~/.m2/maven-central.properties`
   - Required keys:
     - `ossrhUsername`
     - `ossrhPassword`
     - `mavenGpgPassphrase`
   - Optional key:
     - `mavenGpgKeyname`
   - Loader script in this repo:
     - `scripts/load-maven-env.sh`

Load credentials into current shell:

```bash
source scripts/load-maven-env.sh ~/.m2/maven-central.properties
```

## Deploy to Maven Central

For this multi-module build, use the wrapper script so module parent inheritance also uses central settings:

```bash
scripts/deploy-central.sh
```

This script temporarily swaps in `pom-central.xml` as the active parent `pom.xml`, runs:

```bash
mvn -f pom.xml -Pcentral-release clean deploy
```

and then restores your original `pom.xml`.

## Snapshot Deploy

To deploy snapshots via classic OSSRH endpoint:

```bash
mvn -f pom-central.xml clean deploy
```

For snapshots, ensure the version ends with `-SNAPSHOT`.

## Preflight Checks

Run these before a release deploy:

```bash
source scripts/load-maven-env.sh ~/.m2/maven-central.properties
scripts/release-preflight.sh
python3 scripts/ossrh-auth-check.py
```

The OSSRH auth helper is only a compatibility probe for the legacy Sonatype endpoint. A `402` response does not block Central Portal deployment when `scripts/deploy-central.sh` succeeds.

Expected result:

- credentials are loaded and non-empty
- a non-expired signing key is found
- signing key fingerprint is publicly discoverable
- `settings.xml` contains required server ids (`central`, `ossrh`)

## Troubleshooting

If Central deployment fails with:

- `Invalid signature ... Could not find a public key by the key fingerprint`

this is often keyserver propagation delay, even when your key is already uploaded.

Recommended action:

1. Ensure key is discoverable:
   - `curl "https://keys.openpgp.org/vks/v1/by-fingerprint/<FINGERPRINT>"`
   - `curl "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x<FINGERPRINT>"`
2. Wait and retry deploy (typically 15–120 minutes; can be longer).
3. Retry:
   - `source scripts/load-maven-env.sh ~/.m2/maven-central.properties`
   - `scripts/deploy-central.sh`