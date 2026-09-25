WO-401 — Test Harness & CI/CD Pipeline

Status: Completed

Evidence:
- Automated tests pass locally with Maven.
- GitHub Actions CI runs on Pull Requests.
- CI uses JDK 22.
- CI executes mvn clean test.
- Intentional test failure produced a failed CI run.
- Test was restored and CI passed again.
- Branch ruleset requires Pull Request and required CI status check.
- PR #17 was reviewed and merged.

Acceptance Criteria:
- AC1 Automated Tests: PASS
- AC2 CI Pipeline: PASS
- AC3 Branch Protection: PASS
- AC4 Evidence: PASS