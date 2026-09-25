# End-to-End Engineering Delivery Path

## Scope

This document models the delivery path evidenced by the repository. It distinguishes controls that exist in source/configuration/documentation from stages that are only partially evidenced or not defined. It does not introduce a new delivery process.

## Delivery Flow

```text
Requirement
  -> Issue
  -> Work Order
  -> Branch
  -> AI-assisted implementation
  -> Tests
  -> CI
  -> PR
  -> AI review
  -> Human review
  -> Security review
  -> Merge
  -> Release
  -> Verification
  -> Evidence
  -> Close-out
  -> Knowledge accumulation
```

## Stage 1 — Requirement

- **Purpose:** Define the business behavior and scope that delivery must satisfy.
- **Input:** Business need for the IT support ticket system.
- **Output:** `docs/Requirements-IT-Support-Ticket-Document.md`, including eight requirements, in-scope behaviors, and out-of-scope behaviors.
- **Owner:** Not explicitly defined in the repository.
- **Automated control:** Requirement traceability and test design are documented in `docs/test-design.md`; no automated requirement parser or gate exists.
- **Human control:** Human review is required to validate the specification and challenge invented behavior, as stated in `docs/ai-human-review.md` and `CONTRIBUTING.md`.
- **Evidence:** `docs/Requirements-IT-Support-Ticket-Document.md`, `docs/test-design.md`.
- **Failure condition:** The implementation or tests do not match the source requirements, or introduce out-of-scope behavior.

## Stage 2 — Issue

- **Purpose:** Track a problem, expected behavior, scope, and acceptance criteria before implementation.
- **Input:** A requirement or change request.
- **Output:** A GitHub Issue or equivalent task description.
- **Owner:** Contributor/team ownership is not named explicitly.
- **Automated control:** No repository automation validates issue completeness.
- **Human control:** `CONTRIBUTING.md` requires issue-first work and explicit problem, behavior, and acceptance criteria.
- **Evidence:** `CONTRIBUTING.md`; WO-401 is documented as the relevant work item in `docs/WO-401-evidence.md`.
- **Failure condition:** Work starts without a tracked scope or acceptance criteria, or the change exceeds the issue scope.

## Stage 3 — Work Order

- **Purpose:** Convert the tracked issue into a bounded engineering objective with verification evidence.
- **Input:** The issue and its acceptance criteria.
- **Output:** WO-401 scope covering test harness, CI/CD, test isolation, evidence, and quality gates.
- **Owner:** Not explicitly defined in the repository.
- **Automated control:** Maven tests and CI execute the technical verification described by the work order.
- **Human control:** Human review validates that implementation and evidence remain within WO-401 scope.
- **Evidence:** `docs/WO-401-evidence.md`, `docs/pr-quality-gates.md`.
- **Failure condition:** The work order is marked complete without passing verification or without evidence for its acceptance criteria.

## Stage 4 — Branch

- **Purpose:** Isolate work before it is proposed for merge.
- **Input:** An issue/work order and the repository baseline.
- **Output:** A task-specific Git branch and commits, when used.
- **Owner:** Contributor.
- **Automated control:** No branch creation or naming automation is present.
- **Human control:** `CONTRIBUTING.md` documents short descriptive branch names and requires scoped changes.
- **Evidence:** Branch naming guidance exists in `CONTRIBUTING.md`; a specific branch for this delivery path is not recorded in the repository documents.
- **Failure condition:** Work is performed without the required issue context, uses an unclear branch, or mixes unrelated changes.

## Stage 5 — AI-Assisted Implementation

- **Purpose:** Accelerate analysis, implementation candidates, tests, gap identification, and documentation while keeping decisions human-controlled.
- **Input:** Requirements, issue/work order, existing code, and repository conventions.
- **Output:** Proposed code, tests, review findings, and documentation changes.
- **Owner:** AI assists; the human engineer remains responsible for the resulting change.
- **Automated control:** Compilation and tests can reject behavior that does not build or pass the configured suite.
- **Human control:** `docs/ai-human-review.md` requires human validation of requirements, generated code, generated tests, security, behavior, and merge decision.
- **Evidence:** `docs/ai-human-review.md`, `CONTRIBUTING.md` AI/Copilot safeguards.
- **Failure condition:** AI output is accepted without diff review, invents unsupported behavior, or is treated as approved solely because tests pass.

## Stage 6 — Tests

- **Purpose:** Verify behavior before CI and provide automated evidence for the requirements.
- **Input:** Production code, test design, and isolated test configuration.
- **Output:** Application, API/integration, and demo initializer test results.
- **Owner:** Engineering team; human reviewer validates test relevance.
- **Automated control:** `mvn clean test` compiles production/test code and runs JUnit/Spring tests through Maven Surefire. Test configuration uses `./target/it-support-ticket-test.db` and disables demo seeding.
- **Human control:** `docs/ai-test-review.md` and `docs/ai-human-review.md` require challenge of coverage, negative cases, authorization, state transitions, persistence, isolation, and false confidence.
- **Evidence:** Existing test classes, `docs/test-design.md`, `docs/ai-test-review.md`, backend test `application.yml`.
- **Failure condition:** Compilation/test failure, untested required behavior being treated as verified, or tests depending on runtime data.

## Stage 7 — CI

- **Purpose:** Re-run the build and tests consistently for proposed changes.
- **Input:** Pull Request or push to `main`/`master`.
- **Output:** GitHub Actions `build` job result.
- **Owner:** GitHub Actions executes; repository maintainers own the workflow configuration.
- **Automated control:** `.github/workflows/ci.yml` checks out the repository, installs Temurin JDK 22, enables Maven cache, and runs `mvn clean test`.
- **Human control:** Maintainers review workflow changes and CI results; the PR template requires the verification command and observed result.
- **Evidence:** `.github/workflows/ci.yml`, `docs/pr-quality-gates.md`, `docs/WO-401-evidence.md`.
- **Failure condition:** Checkout, dependency resolution, compilation, test compilation, or test execution returns a non-zero result.

## Stage 8 — Pull Request

- **Purpose:** Present the scoped change for review and merge decision.
- **Input:** Branch commits linked to the issue/work order.
- **Output:** Pull Request containing summary, scope, verification, and notes.
- **Owner:** Contributor submits; reviewers evaluate.
- **Automated control:** `pull_request` triggers CI; the PR template standardizes summary, scope, verification command, and notes.
- **Human control:** Reviewers inspect the diff, requirements, risks, and CI result.
- **Evidence:** `.github/PULL_REQUEST_TEMPLATE.md`, `.github/workflows/ci.yml`, PR #17 reference in `docs/WO-401-evidence.md`.
- **Failure condition:** Missing scope/evidence, unexpected changes, failed CI, or unresolved review concerns.

## Stage 9 — AI Review

- **Purpose:** Use AI to identify requirement, test, security, and quality risks for human consideration.
- **Input:** Requirements, implementation, tests, configuration, and CI evidence.
- **Output:** Review findings and recommendations; not an approval.
- **Owner:** AI provides analysis; human reviewer evaluates every finding.
- **Automated control:** No automated gate validates AI review quality.
- **Human control:** Human must challenge findings, verify evidence, and decide whether recommendations are valid.
- **Evidence:** `docs/ai-test-review.md`, `docs/owasp-review.md`, `docs/ai-human-review.md`.
- **Failure condition:** AI review is accepted without evidence review, or a generated finding is treated as fact without validation.

## Stage 10 — Human Review

- **Purpose:** Make the accountable engineering decision on requirements, code, tests, security, and delivery readiness.
- **Input:** Pull Request diff, test results, CI result, AI review, and security review.
- **Output:** Approval, rejection, or requested changes.
- **Owner:** Human reviewer/maintainer.
- **Automated control:** Required CI status check can block merge when configured in GitHub.
- **Human control:** `docs/ai-human-review.md` defines the human checklist and explicit approve/reject decision.
- **Evidence:** `CONTRIBUTING.md` human review requirement, `docs/ai-human-review.md`, PR #17 evidence.
- **Failure condition:** Merge approval is inferred from passing tests without human review, or requirements and risks remain unresolved.

## Stage 11 — Security Review

- **Purpose:** Independently assess authorization, identity handling, validation, secrets, data protection, dependencies, CI/CD, and error handling.
- **Input:** Production source, configuration, workflows, tests, and security-relevant documentation.
- **Output:** Security findings with severity, evidence, remediation, and disposition.
- **Owner:** Human security reviewer; AI may assist with analysis.
- **Automated control:** No dedicated security scanner or security gate is configured in the current workflow.
- **Human control:** `docs/owasp-review.md` and `docs/ai-human-review.md` require independent human security review.
- **Evidence:** `docs/owasp-review.md`, `docs/ai-human-review.md`.
- **Failure condition:** A security finding is ignored, secrets or sensitive data are exposed, or passing functional tests is treated as proof of security.

## Stage 12 — Merge

- **Purpose:** Integrate an approved change into the protected target branch.
- **Input:** Reviewed PR, passing CI, required approval, and branch ruleset conditions.
- **Output:** Merged change on the target branch.
- **Owner:** Repository maintainer/reviewer; the exact GitHub role is not defined in repository files.
- **Automated control:** GitHub required status check and branch ruleset are recorded as controls; the workflow supplies the CI check.
- **Human control:** Human approval and merge decision are mandatory.
- **Evidence:** `docs/WO-401-evidence.md` records the required PR, required CI status check, `main` ruleset, and reviewed/merged PR #17.
- **Failure condition:** Required check fails, approval is missing, ruleset conditions are not met, or a human rejects the change.

## Stage 13 — Release

- **Purpose:** Make an approved, merged change available as a released application version.
- **Input:** Merged code.
- **Output:** Release artifact, deployment, tag, or release record.
- **Owner:** Not defined in the repository.
- **Automated control:** No release workflow, packaging publication, deployment job, or release command was found in the reviewed repository.
- **Human control:** No release approval process is documented in the reviewed repository.
- **Evidence:** No release evidence found. The repository documents local Spring Boot execution and Maven verification, not a production release mechanism.
- **Failure condition:** A release is claimed without a documented artifact, deployment result, or release record.

## Stage 14 — Verification

- **Purpose:** Confirm the delivered change behaves as intended after implementation and CI.
- **Input:** Local Maven result, GitHub Actions result, and the merged change.
- **Output:** Verification result tied to acceptance criteria.
- **Owner:** Engineering team and human reviewer.
- **Automated control:** `mvn clean test` and the GitHub Actions `build` job.
- **Human control:** Human reviewer checks observed results against requirements and acceptance criteria.
- **Evidence:** `docs/WO-401-evidence.md` records passing local tests, CI execution, an intentional failure, restoration, and passing CI; `docs/pr-quality-gates.md` describes the gates.
- **Failure condition:** Verification is missing, stale, or does not cover the claimed acceptance criteria.

## Stage 15 — Evidence

- **Purpose:** Preserve enough information to support the delivery and acceptance decision.
- **Input:** Test results, CI status, PR/review outcome, security findings, and requirement mapping.
- **Output:** Repository evidence documents and PR records.
- **Owner:** Contributor prepares evidence; human reviewer validates it.
- **Automated control:** CI produces pass/fail status; no automatic evidence index or artifact publication is configured.
- **Human control:** Human checks that evidence is current, accurate, and not overstated.
- **Evidence:** `docs/WO-401-evidence.md`, `docs/test-design.md`, `docs/pr-quality-gates.md`, `docs/ai-test-review.md`, `docs/owasp-review.md`.
- **Failure condition:** Evidence claims behavior not implemented, omits failed verification, or cannot be traced to a command, review, PR, or configuration.

## Stage 16 — Close-out

- **Purpose:** Mark the work complete after verification and required review are complete.
- **Input:** Passing verification, review decisions, and merge evidence.
- **Output:** Completed work-order status and issue close-out.
- **Owner:** Not explicitly defined in the repository.
- **Automated control:** No issue-close or work-order-close automation was found.
- **Human control:** `CONTRIBUTING.md` states that an issue is closed only after verification is complete.
- **Evidence:** `docs/WO-401-evidence.md` records WO-401 status as `Completed`; no separate issue-close event is present in the repository.
- **Failure condition:** Work is marked complete without current verification or without resolving open review/security findings.

## Stage 17 — Knowledge Accumulation

- **Purpose:** Preserve reusable engineering learning for later requirement, test, security, and delivery work.
- **Input:** Review findings, gaps, CI evidence, and delivery decisions.
- **Output:** Repository documentation and updated engineering guidance.
- **Owner:** Engineering team; no individual owner is defined.
- **Automated control:** No automated retrospective or knowledge-indexing control was found.
- **Human control:** Humans decide which findings and process lessons are accurate and worth retaining.
- **Evidence:** `docs/ai-human-review.md`, `docs/ai-test-review.md`, `docs/owasp-review.md`, `docs/pr-quality-gates.md`, and `docs/WO-401-evidence.md` accumulate guidance and evidence.
- **Failure condition:** Lessons are not recorded, are recorded without evidence, or become inconsistent with the implementation.

## Existing Controls

- Requirements and explicit out-of-scope behavior are documented.
- Issue-first workflow, scoped changes, branch naming, verification, and human review are documented in `CONTRIBUTING.md`.
- AI output is defined as a proposal and human approval is required in `docs/ai-human-review.md`.
- Test design maps requirements to scenarios and existing tests in `docs/test-design.md`.
- API integration tests use real Spring/SQLite wiring and test the main ticket lifecycle.
- Test SQLite storage is isolated under `target`, and demo initialization is disabled for tests.
- GitHub Actions runs for Pull Requests and uses JDK 22 with `mvn clean test`.
- Maven failures propagate through the CI job.
- PR template requires summary, scope, verification command, result, risks, and follow-up notes.
- Security review findings are documented in `docs/owasp-review.md`.
- WO-401 evidence records intentional CI failure, restoration, required CI status check, branch ruleset, and PR #17 review/merge evidence.

## Missing Controls

- No repository-managed release workflow, artifact publication, deployment verification, or release record.
- No machine-readable branch protection/ruleset configuration in the repository; enforcement is external GitHub settings.
- No automated close-out or issue-state synchronization.
- No automated knowledge-base indexing or retrospective capture.
- No committed Maven Wrapper or Maven runtime version pin.
- No automatic evidence artifact/index generation.
- No dedicated security, dependency, coverage, or lint gate in the current CI workflow.

## Recommended Improvements

These are recommendations, not existing process claims:

1. Add a documented release and post-release verification process before production delivery is required.
2. Manage branch protection through auditable GitHub settings or approved infrastructure-as-code, while retaining the required CI check.
3. Pin the Maven runtime with a Maven Wrapper when cross-runner reproducibility becomes important.
4. Define who owns issue/work-order close-out and record the close event with verification evidence.
5. Establish a lightweight human-reviewed knowledge-retention convention for recurring test, security, and CI findings.
6. Preserve CI logs or test reports as evidence where the project requires durable auditability.
