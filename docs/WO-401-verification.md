# WO-401 Verification

## Section 7 — Test Harness and CI/CD

### 1. Objective

- Provide an automated test harness for the application.
- Validate changes in Continuous Integration before Pull Requests are merged.

### 2. Test Types

The repository includes the following test types:

- Application test
- Integration test
- Demo data initializer test

### 3. Local Verification

- Maven command: `mvn clean test`
- Java version: 22
- SQLite test database isolation is configured for test execution.
- Result: all tests passed.

### 4. CI Verification

The GitHub Actions workflow in `.github/workflows/ci.yml`:

- Runs on Pull Request events.
- Uses JDK 22 through Temurin.
- Executes `mvn clean test`.

### 5. Failure Verification

- A test assertion was intentionally changed to an incorrect expected value.
- The local Maven test run failed.
- The corresponding GitHub Actions validation failed.
- The test assertion was restored to the correct expected value.
- GitHub Actions passed again after the restoration.

### 6. Quality Gates

- A Pull Request is required before merging.
- The required CI status check must pass.
- Branch protection/ruleset requirements apply to `main`.

### 7. PR Evidence

- Reference: PR #17.
- PR #17 was reviewed and merged.

### 8. Conclusion

The repository now has an automated verification gate that runs the test suite for Pull Requests before changes are merged. This provides repeatable local and CI validation for the application test, integration test, and demo data initializer test coverage.
