# WO-401 Lessons Learned

## Evidence Scope

This knowledge record is derived from the following repository evidence:

- `docs/WO-401-evidence.md`
- `.github/workflows/ci.yml`
- `docs/test-design.md`
- `docs/ai-test-review.md`
- `docs/owasp-review.md`
- `docs/pr-quality-gates.md`
- `docs/ai-human-review.md`
- `.github/PULL_REQUEST_TEMPLATE.md`
- `CONTRIBUTING.md`

No lesson is recorded unless the reviewed evidence supports it.

## 1. What Happened

- WO-401 established an automated backend test harness using Maven, JUnit/Spring tests, SQLite, and GitHub Actions.
- The CI workflow runs for Pull Requests and pushes to `main` and `master`.
- CI sets up JDK 22 and runs `mvn clean test`.
- Test configuration was isolated from the runtime SQLite database by using `./target/it-support-ticket-test.db` and disabling demo-data initialization for tests.
- Existing API integration tests were reviewed and extended to cover validation, authorization, state transitions, structured errors, and the complete ticket lifecycle.
- A controlled test failure was introduced by changing one expected ticket status from `OPEN` to `RESOLVED`, then the assertion was restored.
- WO-401 evidence records the intentional local/CI failure, the restoration, passing CI, required status checks, branch ruleset, and reviewed/merged PR #17.

## 2. What Worked

- `mvn clean test` provides a single repeatable command for dependency resolution, compilation, test compilation, and automated test execution.
- The Pull Request trigger makes the test command part of the proposed-change path.
- The JDK version is explicit in CI as Java 22.
- The isolated SQLite test path prevents integration tests from using the developer runtime database.
- Disabling demo data in test configuration prevents startup seed data from contaminating API integration tests.
- Real `MockMvc` integration tests exercise the controller, service, repositories, and SQLite together for the main workflow.
- The lifecycle test verifies `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED` through the API.
- The intentional assertion failure proved that a test failure propagates to Maven and CI.
- Human-review guidance explicitly requires requirement, code, test, security, and CI review.
- Test design and security review documents make gaps visible instead of treating passing tests as complete proof.

## 3. What Failed

- The intentionally incorrect assertion caused the targeted test and CI build to fail, as expected.
- The failure demonstrated that a passing CI result depends on the correctness of test assertions, not only on workflow execution.
- The test review found coverage gaps for mismatched technician identity, missing/invalid identity headers, invalid resolution states, environment-driven database-path selection, and demo seed content.
- The test review found false-confidence risks in mock-count-only demo initializer assertions and the `assertTrue(true)` application smoke test.
- The CI/reproducibility review found that Maven itself is not pinned and branch protection is not machine-readable in the repository.
- The security review found no dedicated dependency/security scan in the current CI workflow.

## 4. Why It Happened

- The controlled failure was caused by an intentionally changed expected value; it was not an uncontrolled production defect.
- Test gaps existed because the suite focused on the main workflow and selected negative cases rather than a complete requirement/state/identity matrix.
- Demo initializer tests use Mockito repository interactions, so they verify calls and counts rather than persisted seed values and relationships.
- The application smoke test was implemented as a no-op assertion, so it does not load the application or verify a requirement.
- CI invokes `mvn` directly, and the repository does not contain a Maven Wrapper.
- Branch ruleset enforcement is configured outside the repository in GitHub settings; the repository contains documentation evidence rather than a machine-readable ruleset.
- The current workflow intentionally remains focused on `mvn clean test` and does not configure coverage, dependency, or security scanners.

## 5. What Was Learned

- A clean CI run is meaningful only when tests are traced to requirements and the assertions are reviewed by a human.
- Deliberately breaking one assertion is a practical way to verify failure propagation and the CI evidence path.
- Test isolation must be explicit; transaction rollback alone should not be treated as proof that runtime data is protected.
- End-to-end API tests are valuable for the ticket lifecycle because they verify observable responses and state progression through real wiring.
- Test count is not a quality metric. A no-op smoke test can inflate counts without increasing confidence.
- Mock interaction counts are weaker evidence than assertions over generated values, relationships, and persisted behavior.
- Security review must remain independent from functional test results; passing `mvn clean test` does not prove authorization, identity authenticity, or data protection.
- Human review is required to distinguish implementation-specific behavior from documented requirements.
- CI provides a pass/fail gate, but reproducibility and auditability also depend on toolchain pinning and retained evidence.

## 6. What Rule Should Be Created

The following rules are directly supported by the WO-401 review evidence:

- Every work order must identify its source requirements and acceptance criteria before implementation.
- Every in-scope requirement must map to a positive scenario, a relevant negative scenario, an automated test or an explicit coverage gap.
- AI-generated code and tests must be reviewed by a human before merge.
- A controlled failure check should be performed when a new CI gate is introduced or materially changed.
- Integration tests must use test-specific storage and must not use runtime data.
- A test must assert observable behavior or meaningful domain values; interaction counts alone are insufficient for persisted business data.
- Security review must be recorded separately from functional test results.
- A Pull Request must include the verification command and observed result.
- Passing generated tests must never be treated as automatic approval for merge.

## 7. What Should Become a Template

The following existing documents are reusable templates or should be kept as templates:

- PR summary, scope, verification, and risk sections from `.github/PULL_REQUEST_TEMPLATE.md`.
- Requirement-to-test mapping structure from `docs/test-design.md`.
- Finding format and evidence/status distinction from `docs/ai-test-review.md` and `docs/owasp-review.md`.
- AI/human responsibility checklist from `docs/ai-human-review.md`.
- CI quality-gate structure from `docs/pr-quality-gates.md`.
- Work-order evidence structure from `docs/WO-401-evidence.md`.

These templates should remain evidence-oriented and should not claim controls that are not configured or verified.

## 8. What Should Become Automation

The following automation opportunities are supported by identified gaps, but are not currently implemented:

- Pin Maven with a committed Maven Wrapper or an explicitly managed CI Maven version.
- Preserve CI test summaries or Surefire reports as workflow evidence.
- Add an automated requirement/test traceability check only if the team can maintain a stable requirement identifier convention.
- Add dependency vulnerability and secret scanning when the project accepts those additional CI controls.
- Validate that the required CI status check and branch ruleset remain configured through approved GitHub administration or infrastructure-as-code.
- Add structured metadata for AI-assisted PRs and human review, such as an agreed label or checklist field, before measuring that KPI reliably.
- Automate RCA reminders or completion checks only after an RCA record format and ownership rule are defined.

These are recommendations derived from documented gaps, not existing controls.

## 9. What Should Be Reused in Future Work Orders

- Start with the repository requirement document as the source of truth.
- Record a requirement-to-test trace before declaring coverage complete.
- Review existing tests and production behavior before generating additional tests.
- Prefer a focused integration scenario for the main user workflow, supported by targeted negative tests.
- Verify error status, code, message, and path where the API error contract requires them.
- Verify that rejected state-changing operations leave state unchanged.
- Use isolated test data and explicitly disable runtime/demo initialization where appropriate.
- Run the same Maven command locally and in CI.
- Include a controlled failure or equivalent evidence when validating a new gate.
- Record open coverage, security, CI, and reproducibility gaps instead of hiding them behind a passing test count.
- Require human review of requirements, code, tests, security, CI results, and merge readiness.
- Close the work order only after verification evidence and human review are complete.
