# Publish Guide

## Purpose

This guide defines a repeatable release process for the Camel A2A component and sample modules.

## Pre-Release Checklist

1. Working tree is clean (or only intended release changes are present).
2. All tests pass:

```bash
mvn test
mvn -pl camel-a2a-component test
mvn -pl samples/a2a-yaml-service test
```

3. `README.md` and docs reflect current routes and method behavior.
4. Version numbers are set correctly in `pom.xml` files.

## Build Artifacts

From repo root:

```bash
mvn clean package
```

Primary outputs:

- Component JAR: `camel-a2a-component/target/`
- Sample JAR: `samples/a2a-yaml-service/target/`

## Versioning

1. Update parent version in root `pom.xml`.
2. Ensure child modules inherit or explicitly use the release version.
3. Replace `-SNAPSHOT` with the target release version for tagged releases.

## Release Steps

1. Commit release changes.
2. Create and push a release tag (for example `v0.5.0`).
3. Publish artifacts using your configured Maven repository workflow.
4. Create GitHub release notes summarizing:
   - Added methods/features
   - Breaking changes (if any)
   - Verification/test status

## Post-Release

1. Bump to next development version (`x.y.z-SNAPSHOT`).
2. Commit and push post-release version updates.
3. Re-run `mvn test` to confirm repository health.
