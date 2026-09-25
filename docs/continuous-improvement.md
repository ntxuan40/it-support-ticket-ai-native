# Continuous Improvement Rules from WO-401

## Purpose

This document converts evidence-backed WO-401 lessons into engineering rules that can be checked or enforced. It does not create slogans or claim that recommended automation already exists.

Each rule follows:

```text
Lesson -> Rule -> Template -> Automation -> Verification
```

## Rule CI-001 — Requirements Must Map to Verification

- **Rule ID:** CI-001
- **Trigger:** A new Work Order, requirement change, or acceptance-criteria change is started.
- **Problem:** WO-401 found that passing tests do not prove complete requirement coverage; the test design identified partial coverage and explicit gaps.
- **New rule:** Before implementation is declared complete, every in-scope requirement must have a positive scenario, a relevant negative scenario where applicable, and either an automated test reference or an explicit documented gap.
- **Why:** Prevents test count or CI pass status from being mistaken for requirements verification.
- **Enforcement mechanism:** Review the requirement-to-test mapping during PR review; require the mapping in the Work Order evidence package.
- **Evidence:** `docs/test-design.md`, `docs/ai-test-review.md`, `CONTRIBUTING.md` issue/acceptance-criteria workflow.
- **Applicable scope:** All Work Orders that change behavior, tests, API contracts, persistence, or security.
- **Improvement chain:**
  - **Lesson:** Requirement coverage was partial despite a passing suite.
  - **Rule:** Every requirement has a test or explicit gap.
  - **Template:** Reuse `docs/test-design.md` structure.
  - **Automation:** Optional future check for stable requirement IDs and referenced test methods.
  - **Verification:** Human reviewer checks the traceability before approval.

## Rule CI-002 — Test Failures Must Be Proven to Propagate

- **Rule ID:** CI-002
- **Trigger:** A new CI test gate or materially changed test workflow is introduced.
- **Problem:** A CI gate can appear configured without evidence that a failing assertion makes the job fail.
- **New rule:** Perform one controlled failure verification for a new or materially changed CI test gate, record the failed result, restore the test, and record the passing result.
- **Why:** Confirms that the intended failure signal reaches Maven, GitHub Actions, and the merge gate.
- **Enforcement mechanism:** Work Order evidence must include the failure and restoration records; the PR must not retain the intentional failure.
- **Evidence:** `docs/WO-401-evidence.md`, `docs/pr-quality-gates.md`, `.github/workflows/ci.yml`.
- **Applicable scope:** Changes to CI test commands, required status checks, test harness configuration, or branch merge gates.
- **Improvement chain:**
  - **Lesson:** The deliberately wrong `OPEN`/`RESOLVED` assertion failed locally and in CI, then passed after restoration.
  - **Rule:** Verify failure propagation when the gate changes.
  - **Template:** Reuse the failure evidence structure in `docs/WO-401-evidence.md`.
  - **Automation:** Future CI metadata could link the intentional failure run and restoration run.
  - **Verification:** Confirm one failed run, one restored passing run, and a clean final diff.

## Rule CI-003 — Integration Tests Must Use Isolated Test Storage

- **Rule ID:** CI-003
- **Trigger:** A test creates or reads persisted application data.
- **Problem:** The original integration test configuration shared the runtime SQLite path and created the runtime data directory from test code.
- **New rule:** Integration tests must use a test-specific database location and must not create, read, or modify the runtime database path.
- **Why:** Prevents developer data contamination and makes local/CI execution deterministic.
- **Enforcement mechanism:** Test configuration points to `./target/it-support-ticket-test.db`; demo initialization is disabled for tests; review rejects runtime database paths in test code/configuration.
- **Evidence:** `it-support-ticket-app/backend/src/test/resources/application.yml`, `TicketControllerIntegrationTest.java`, `docs/pr-quality-gates.md`.
- **Applicable scope:** Integration, persistence, repository, and startup tests using SQLite.
- **Improvement chain:**
  - **Lesson:** Runtime and test SQLite paths were initially shared.
  - **Rule:** Use isolated test storage and disable runtime/demo initialization in tests.
  - **Template:** Reuse the test `application.yml` pattern.
  - **Automation:** Add a future check that rejects runtime `./data` paths in test resources and test source.
  - **Verification:** Confirm the test path is under `target`, the runtime path is not referenced, and tests pass after `mvn clean test`.

## Rule CI-004 — Test Assertions Must Verify Behavior, Not Only Interactions

- **Rule ID:** CI-004
- **Trigger:** A test is generated or modified for business data, persistence, or initialization.
- **Problem:** Demo initializer tests verified repository save counts but not seeded values, relationships, statuses, or priorities; this creates false confidence.
- **New rule:** Tests for persisted business behavior must assert meaningful output/state/value relationships. Mock interaction counts alone cannot be the only evidence for business data correctness.
- **Why:** A test can pass while the application stores incorrect records if it checks only that methods were called.
- **Enforcement mechanism:** Human test review using the false-confidence checklist; require value assertions or repository-backed verification for seed/persistence behavior.
- **Evidence:** `docs/ai-test-review.md`, `DemoDataInitializerTest.java`, `docs/test-design.md`.
- **Applicable scope:** Persistence tests, demo data tests, repository integration tests, and generated business tests.
- **Improvement chain:**
  - **Lesson:** Mock save counts did not prove demo record correctness.
  - **Rule:** Assert business values and relationships.
  - **Template:** Use the test design fields for expected result and persistence evidence.
  - **Automation:** Add repository-backed test coverage where the initializer data contract becomes important.
  - **Verification:** Review assertions for seed names, roles, links, statuses, priorities, and ticket data where required.

## Rule CI-005 — AI-Generated Code and Tests Require Human Review

- **Rule ID:** CI-005
- **Trigger:** AI assistance is used for requirements analysis, code, tests, documentation, or review findings.
- **Problem:** Generated tests may pass while testing the wrong behavior, missing requirements, or creating false confidence.
- **New rule:** AI output is a proposal and cannot be accepted or merged without human review of requirements, diff, tests, security, behavior, and CI evidence.
- **Why:** Passing generated tests is not equivalent to engineering approval.
- **Enforcement mechanism:** Human review checklist, PR review, required status check, and branch protection before merge.
- **Evidence:** `docs/ai-human-review.md`, `CONTRIBUTING.md`, `.github/PULL_REQUEST_TEMPLATE.md`, `docs/WO-401-evidence.md`.
- **Applicable scope:** Every AI-assisted Work Order, including code, tests, documentation, and security analysis.
- **Improvement chain:**
  - **Lesson:** Human challenge was needed to identify missing identity, persistence, error-contract, and false-confidence coverage.
  - **Rule:** Human approval is mandatory regardless of generated test results.
  - **Template:** Reuse `docs/ai-human-review.md` and the PR template.
  - **Automation:** Require a PR checklist/label or review state before merge; current repository evidence does not show AI metadata automation.
  - **Verification:** A human reviewer records approval after reviewing the diff and CI result.

## Rule CI-006 — Security Review Is Independent from Functional Test Results

- **Rule ID:** CI-006
- **Trigger:** A Work Order changes authorization, identity, input handling, data storage, error handling, dependencies, or CI/CD.
- **Problem:** Functional tests passing does not prove access control, authentication, secrets protection, dependency safety, or data protection.
- **New rule:** Security-relevant changes require a separate evidence-based security review before merge; CI pass status cannot substitute for that review.
- **Why:** WO-401 identified access-control, identity-header, runtime configuration, CI supply-chain, and SQLite data-protection risks not established by functional tests.
- **Enforcement mechanism:** Attach a security review record to the Work Order/PR and require human disposition of findings.
- **Evidence:** `docs/owasp-review.md`, `docs/ai-human-review.md`, `docs/WO-401-evidence.md`.
- **Applicable scope:** Security-relevant backend, configuration, dependency, database, workflow, and API changes.
- **Improvement chain:**
  - **Lesson:** Security gaps remained even with API tests and CI evidence.
  - **Rule:** Perform an independent security review.
  - **Template:** Reuse the finding format in `docs/owasp-review.md`.
  - **Automation:** Future dependency/secret scanning may supplement, but not replace, human review.
  - **Verification:** Confirm findings have evidence, severity, remediation, and disposition before merge.

## Rule CI-007 — Pull Requests Must Carry Reproducible Verification Evidence

- **Rule ID:** CI-007
- **Trigger:** A Pull Request proposes a repository change.
- **Problem:** Reviewers cannot evaluate delivery confidence from a generic “tests pass” claim without the exact command and observed result.
- **New rule:** Every Pull Request must state the exact verification command, observed result, related requirement/work item, risks, and follow-up items.
- **Why:** Creates a reviewable link between change scope, requirements, local verification, and CI evidence.
- **Enforcement mechanism:** `.github/PULL_REQUEST_TEMPLATE.md`; human review rejects missing or stale verification evidence.
- **Evidence:** `.github/PULL_REQUEST_TEMPLATE.md`, `CONTRIBUTING.md`, `docs/pr-quality-gates.md`.
- **Applicable scope:** All Pull Requests, including documentation-only changes where verification is relevant.
- **Improvement chain:**
  - **Lesson:** WO-401 required explicit evidence for local pass, CI pass/fail, restoration, and merge controls.
  - **Rule:** PRs record reproducible verification evidence.
  - **Template:** Reuse the existing PR template and `docs/WO-401-evidence.md` structure.
  - **Automation:** Required CI status check enforces executable verification; evidence completeness remains human-controlled.
  - **Verification:** Reviewer confirms command/result and matches them to the changed scope.

## Rule CI-008 — Toolchain Reproducibility Must Be Explicit When Required

- **Rule ID:** CI-008
- **Trigger:** A Work Order depends on build reproducibility across local and CI environments, or the CI runner/toolchain changes.
- **Problem:** CI pins JDK 22 but invokes an unpinned `mvn`; no Maven Wrapper is present.
- **New rule:** When cross-environment Maven reproducibility is a requirement, the repository must pin Maven through a committed wrapper or an explicitly managed CI toolchain.
- **Why:** A future runner image can provide a different Maven version and change build/dependency behavior.
- **Enforcement mechanism:** Review the presence/version of `mvnw` or the managed CI Maven installation; update the PR quality-gate evidence.
- **Evidence:** `.github/workflows/ci.yml`, root/backend POMs, `docs/pr-quality-gates.md` Gap 1.
- **Applicable scope:** CI/toolchain changes and Work Orders requiring reproducible builds across environments.
- **Improvement chain:**
  - **Lesson:** Java was pinned, but Maven was not.
  - **Rule:** Pin Maven when reproducibility is required.
  - **Template:** Add toolchain verification to the PR quality-gate checklist.
  - **Automation:** Use a committed Maven Wrapper or managed runner toolchain.
  - **Verification:** Confirm local and CI use the same Maven version and run `mvn clean test`/wrapper equivalent.

## Rule CI-009 — Work Orders Must Record Open Gaps Instead of Hiding Them

- **Rule ID:** CI-009
- **Trigger:** A Work Order reaches verification or close-out.
- **Problem:** WO-401 found incomplete authorization, transition, persistence, demo-data, security, and reproducibility coverage even though the main suite passed.
- **New rule:** Close-out evidence must list unresolved test, security, CI, and process gaps separately from passed controls; a passing test count cannot close an unverified requirement silently.
- **Why:** Makes residual risk visible and supports a deliberate human acceptance decision.
- **Enforcement mechanism:** Work Order evidence template and human close-out review.
- **Evidence:** `docs/WO-401-evidence.md`, `docs/test-design.md`, `docs/ai-test-review.md`, `docs/owasp-review.md`.
- **Applicable scope:** All Work Orders with automated verification or AI-assisted delivery.
- **Improvement chain:**
  - **Lesson:** Passing tests coexisted with documented gaps and false-confidence risks.
  - **Rule:** Separate passed controls from open gaps at close-out.
  - **Template:** Reuse the gaps sections in the review documents and WO evidence.
  - **Automation:** Future evidence tooling may validate that a gap disposition is present before close-out.
  - **Verification:** Human reviewer confirms every requirement is passed, accepted as risk, or explicitly left open.

## Rule CI-010 — CI Status and Branch Protection Must Be Verified Together

- **Rule ID:** CI-010
- **Trigger:** A CI workflow, required status check, branch ruleset, or merge policy changes.
- **Problem:** The workflow provides the CI signal, while branch protection is configured externally and is not machine-readable in the repository.
- **New rule:** A merge gate is considered verified only when the CI job name, required status check, Pull Request requirement, and target branch ruleset are confirmed together.
- **Why:** A passing workflow alone does not guarantee that merging is blocked when the check fails.
- **Enforcement mechanism:** GitHub branch ruleset plus documented verification evidence; keep the required job name stable.
- **Evidence:** `.github/workflows/ci.yml`, `docs/pr-quality-gates.md` Gap 2, `docs/WO-401-evidence.md`.
- **Applicable scope:** CI workflow and repository governance changes.
- **Improvement chain:**
  - **Lesson:** Branch protection evidence was external to repository files.
  - **Rule:** Verify workflow signal and GitHub merge enforcement together.
  - **Template:** Reuse the Merge Gate and Evidence sections in `docs/pr-quality-gates.md`.
  - **Automation:** Manage rulesets through approved GitHub administration or infrastructure-as-code if required.
  - **Verification:** Use a failing and passing PR/check to confirm merge blocking and release of the block.

## Rule Adoption Summary

| Rule | Current enforcement | Reusable artifact | Verification status |
| --- | --- | --- | --- |
| CI-001 | Human review/documentation | `docs/test-design.md` | Existing, human-enforced |
| CI-002 | Work Order evidence | `docs/WO-401-evidence.md` | Proven by WO-401 |
| CI-003 | Test configuration | Test `application.yml` | Existing, configuration-enforced |
| CI-004 | Test review | `docs/ai-test-review.md` | Existing, human-enforced |
| CI-005 | PR/human review | `docs/ai-human-review.md`, PR template | Existing, human-enforced |
| CI-006 | Security review document | `docs/owasp-review.md` | Existing, human-enforced |
| CI-007 | PR template and CI | `.github/PULL_REQUEST_TEMPLATE.md` | Existing, partially automated |
| CI-008 | No current wrapper/toolchain pin | `docs/pr-quality-gates.md` | Recommended, not implemented |
| CI-009 | Work Order/review evidence | Review documents | Existing, human-enforced |
| CI-010 | GitHub ruleset plus workflow | `ci.yml`, quality-gate evidence | External configuration required |
