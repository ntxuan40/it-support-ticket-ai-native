# Pull Request Quality Gates

## Trigger

GitHub Actions workflow `.github/workflows/ci.yml` runs for:

- Pull Requests through the `pull_request` trigger.
- Pushes to `main` and `master` through the configured `push` trigger.

The Pull Request trigger is the relevant gate for proposed changes.

## Verification

The `build` job runs on `ubuntu-latest` and performs these steps:

1. Checks out the repository with `actions/checkout@v4`.
2. Installs Temurin JDK 22 with `actions/setup-java@v4`.
3. Enables Maven dependency caching through the setup action.
4. Runs `mvn clean test`.

The Maven reactor includes the root project and `it-support-ticket-app/backend`. The backend POM provides the Spring Boot test stack, JPA, SQLite JDBC driver, and test configuration required by the existing test suite.

`mvn clean test` performs the effective verification sequence for this project:

- Maven dependency resolution.
- Java compilation.
- Test compilation.
- Automated JUnit/Spring tests through Maven Surefire.
- Clean-build execution from a removed `target` directory.

The test configuration uses the isolated SQLite file `./target/it-support-ticket-test.db` and disables demo-data initialization for tests. Runtime SQLite data under `./data` is not used by the test configuration.

## Failure Gate

A Pull Request fails the CI job when any command in the workflow exits unsuccessfully. In the current workflow, the decisive command is:

```bash
mvn clean test
```

Therefore, any dependency-resolution error, compilation error, test compilation error, test failure, test error, or Maven/Surefire failure propagates as a failed job because Maven returns a non-zero exit status.

The repository evidence in `../08-incident-learning/WO-401-evidence.md` records an intentional incorrect test assertion that caused local Maven and GitHub Actions failures, followed by restoration of the assertion and a passing CI result.

## Success Gate

A Pull Request passes the workflow when:

- The runner can check out the repository.
- JDK 22 is installed successfully.
- Maven resolves the project dependencies.
- The project compiles successfully.
- All Maven test phases complete successfully.
- No test failures or errors cause `mvn clean test` to return non-zero.

There is no separate lint, coverage, dependency-audit, or security-scan job in the current workflow. No additional scanner is required by the reviewed CI requirement.

## Merge Gate

The repository evidence in `../08-incident-learning/WO-401-evidence.md` states that:

- A Pull Request is required before merging.
- A required CI status check is required.
- A branch ruleset applies to `main`.
- PR #17 was reviewed and merged after the verification evidence was completed.

The checked-in workflow supplies the CI status; the enforcement of the required check and branch ruleset is a GitHub repository setting rather than a file in `.github/workflows/`.

## Evidence

- **PR trigger:** `.github/workflows/ci.yml` declares `pull_request`.
- **JDK:** `.github/workflows/ci.yml` configures Temurin Java 22.
- **Maven verification:** `.github/workflows/ci.yml` runs `mvn clean test`.
- **Dependency resolution:** Maven resolves dependencies declared in the root and backend POMs; setup-java enables Maven caching.
- **Compilation and tests:** `mvn clean test` includes compile, test compilation, and Surefire execution through the Maven reactor.
- **Test isolation:** `it-support-ticket-app/backend/src/test/resources/application.yml` uses `./target/it-support-ticket-test.db` and disables demo data.
- **Failure propagation:** `../08-incident-learning/WO-401-evidence.md` records a deliberate assertion failure in local Maven and GitHub Actions, followed by a restored passing state.
- **Required check and branch ruleset:** `../08-incident-learning/WO-401-evidence.md` records the required CI status check, Pull Request requirement, and `main` ruleset.
- **PR evidence:** `../08-incident-learning/WO-401-evidence.md` references PR #17 as reviewed and merged.
- **Existing PR configuration:** `.github/PULL_REQUEST_TEMPLATE.md` requests a summary, scope, and `mvn clean test` verification.

## Gap

### Gap 1 — Maven version is not pinned

- **Gap:** The workflow pins Java 22 but does not pin the Maven version. No Maven Wrapper is present in the reviewed repository, and the POM does not define a Maven runtime version.
- **Evidence:** `.github/workflows/ci.yml` invokes `mvn clean test` directly; the reviewed root and backend POMs define project/compiler/plugin versions but not the Maven distribution version.
- **Risk:** A future runner image change could supply a different Maven version and produce different dependency or build behavior.
- **Recommendation:** Pin Maven through a committed Maven Wrapper or an explicitly managed CI toolchain when reproducibility across runner images becomes a requirement. This is not required to preserve the current `mvn clean test` gate.

### Gap 2 — Branch protection is not machine-readable in the repository

- **Gap:** The required status check and `main` branch ruleset cannot be independently verified from the checked-in files.
- **Evidence:** No ruleset or branch-protection file was found under the repository; the configuration is recorded in `../08-incident-learning/WO-401-evidence.md` as GitHub repository evidence.
- **Risk:** Repository documentation can become stale if GitHub settings change, potentially allowing merges without the intended CI check.
- **Recommendation:** Verify the GitHub branch ruleset in repository settings and keep the CI job name stable so the required status check remains correctly bound. No workflow change is necessary based on the current evidence.

No additional gap is recorded for PR triggering, JDK 22, Maven dependency resolution, compilation, automated tests, or failure propagation because the current workflow and documented evidence show those gates are present.
