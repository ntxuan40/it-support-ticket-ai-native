# AI-Generated Test Review

## Review Context

This review applies the principle that AI-generated tests must be reviewed, challenged, and verified by humans. The review compares:

- `../01-requirements/Requirements-IT-Support-Ticket-Document.md`
- `../04-design/test-design.md`
- Existing backend test classes
- The controller, service, exception handler, state machine, and demo initializer covered by those tests
- The Maven and SQLite test configuration

Status values used in this document:

- **Verified:** The current test evidence directly demonstrates the stated behavior.
- **Suspected:** The test setup creates a credible risk of false confidence, but this review does not claim a production defect.
- **Not tested:** No existing automated test demonstrates the behavior.
- **Not applicable:** The review dimension does not apply to the cited test or requirement.

## Findings

### F-001 — Core API happy paths are verified

- **Category:** Requirement correctness / Positive scenarios
- **Evidence:** `TicketControllerIntegrationTest` exercises ticket creation, ticket retrieval, assignment, start work, resolution, and a complete `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED` flow through `MockMvc`. The requests use the real Spring context, repositories, service, controller, and SQLite datasource.
- **Risk:** Low for the covered happy paths.
- **Impact:** The primary in-scope workflow has meaningful API-level verification rather than only unit-level mocks.
- **Recommendation:** Retain the end-to-end lifecycle test as the primary regression test for the supported workflow.
- **Priority:** P2
- **Status:** Verified

### F-002 — Requirement 1 is covered for the main create behavior, but rejection persistence is not proven

- **Category:** Requirement correctness / Persistence
- **Evidence:** The suite verifies successful creation, `OPEN` status, blank title rejection, missing description rejection, and a missing-priority default. The negative tests assert the HTTP response but do not query the repository after rejection to prove no ticket was stored.
- **Risk:** A future implementation could return a validation error after a partial write and the current tests could still pass.
- **Impact:** The test suite may provide false confidence about the no-partial-create behavior implied by rejecting invalid input.
- **Recommendation:** Add a focused persistence assertion for rejected creation only if the requirement is interpreted as requiring no stored invalid ticket.
- **Priority:** P1
- **Status:** Not tested

### F-003 — Requirement 2 assignment behavior is substantially covered, with incomplete state/target combinations

- **Category:** Requirement correctness / Negative scenarios / State transitions
- **Evidence:** Existing tests cover valid Tech Lead assignment, employee rejection, non-technician target rejection, duplicate assignment, and assignment in the lifecycle test. They do not cover every invalid current state or all possible target conditions.
- **Risk:** Assignment guards could regress for an unrepresented ticket state while the current suite remains green.
- **Impact:** Only the tested assignment paths are protected; the “single technician” rule is not exercised across a complete state matrix.
- **Recommendation:** Add only the invalid-state scenarios that are explicitly needed for the supported lifecycle; avoid broad combinations that are not requirements.
- **Priority:** P2
- **Status:** Partial

### F-004 — Requirement 3 work progression has important authorization and state gaps

- **Category:** Authorization / State transitions / Negative scenarios
- **Evidence:** Tests cover assigned-technician start, rejection of start from `OPEN`, successful resolution, and blank resolution-note rejection. No test uses a different technician identity to attempt start or resolve, and no test attempts resolution from `OPEN` or `ASSIGNED`.
- **Risk:** An authorization or transition guard could be weakened without detection.
- **Impact:** The suite does not fully prove that only the assigned technician can progress or resolve work and that resolution requires `IN_PROGRESS`.
- **Recommendation:** Add focused API tests for wrong-technician start/resolve and one invalid resolution state if those scenarios remain in scope.
- **Priority:** P1
- **Status:** Not tested

### F-005 — Requirement 4 has a valid lifecycle test but not a complete invalid-transition matrix

- **Category:** State transitions / Requirement correctness
- **Evidence:** The lifecycle test verifies the supported sequence. Invalid coverage verifies starting an `OPEN` ticket and assigning an already assigned ticket. There is no direct test for attempting to modify a `RESOLVED` ticket or for every other invalid transition.
- **Risk:** A transition rule could be accidentally broadened while the happy-path lifecycle continues to pass.
- **Impact:** Terminal-state and shortcut-transition behavior are not fully protected.
- **Recommendation:** Add a small set of representative invalid transitions, prioritizing `RESOLVED` as terminal and direct `OPEN -> RESOLVED` behavior if exposed by the API.
- **Priority:** P1
- **Status:** Partial

### F-006 — Role headers are exercised, but header and identity boundaries are incomplete

- **Category:** Authorization / API behavior
- **Evidence:** Positive tests send `X-User-Id` and `X-User-Role`; an employee assignment attempt is rejected. `TicketController.parseRole` rejects blank and invalid roles, and `TicketService` validates actor identity for state-changing operations, but no existing test invokes those missing/invalid-header branches. The `GET` controller method does not receive identity headers in its signature, although the existing GET test sends them.
- **Risk:** Header parsing and identity enforcement could regress without a failing test. A passing GET test does not prove authorization because the test does not demonstrate that the headers affect access.
- **Impact:** Requirement 5 is only partially verified, especially for unauthorized and malformed caller context.
- **Recommendation:** Add focused tests for missing/invalid role headers and mismatched technician identity. Treat GET authorization separately because the current controller boundary does not expose a header-based check.
- **Priority:** P1
- **Status:** Partial

### F-007 — Error contract assertions are inconsistent

- **Category:** Error contract / Validation
- **Evidence:** Several tests assert HTTP status, error code, message, and path, including blank title, missing description, employee assignment, non-technician assignment, invalid start, duplicate assignment, blank resolution note, and malformed JSON. Other tests, including invalid priority and not-found retrieval, assert only status/error code.
- **Risk:** The response shape or request path could regress for partially asserted error cases without detection.
- **Impact:** Requirement 6's structured-error claim is not uniformly enforced across negative scenarios.
- **Recommendation:** Strengthen only representative existing negative tests where the response contract is important; do not invent fields beyond the actual `ApiErrorResponse` fields.
- **Priority:** P2
- **Status:** Partial

### F-008 — Integration tests use observable HTTP behavior rather than controller mocks

- **Category:** Behavior vs implementation / API
- **Evidence:** `TicketControllerIntegrationTest` uses `@SpringBootTest`, `@AutoConfigureMockMvc`, real repositories, real service wiring, and HTTP requests through `MockMvc`. It asserts response status and JSON fields rather than private method calls or mock interactions.
- **Risk:** Low for the controller/API slice covered by these tests.
- **Impact:** The tests are appropriately resistant to internal refactoring that preserves the API behavior.
- **Recommendation:** Preserve this integration style for API contract and workflow tests. Keep Mockito limited to the initializer unit tests where repository isolation is intentional.
- **Priority:** P2
- **Status:** Verified

### F-009 — Demo initializer tests can create false confidence through interaction counts

- **Category:** Behavior vs implementation / Demo data / Persistence
- **Evidence:** `DemoDataInitializerTest.run_shouldSeedDemoData_whenEnabledAndEmpty` verifies `save` and `saveAll` call counts. It does not inspect the seeded users, devices, tickets, relationships, statuses, or priorities. The current class also verifies disabled mode but does not use a real database.
- **Risk:** The initializer could call the expected number of repository methods while producing incorrect seed content.
- **Impact:** Requirement 8 is only partially verified; passing tests do not prove usable sample data was created.
- **Recommendation:** Add focused value-based assertions or a repository-backed initializer test for the sample records defined by the production initializer.
- **Priority:** P1
- **Status:** Partial

### F-010 — Persistence is exercised, but durable storage and configuration selection are not proven

- **Category:** Persistence / Configuration
- **Evidence:** The integration suite starts against SQLite and uses repositories to create and read entities. The test configuration points both datasource properties to `./target/it-support-ticket-test.db`; the runtime configuration uses `IT_SUPPORT_TICKET_DB_PATH` with `./data/it-support-ticket.db` as fallback. No test changes the environment variable or verifies the selected path across an application restart.
- **Risk:** The suite can pass while path selection or durable persistence behavior is wrong outside the single test context.
- **Impact:** Requirement 7 has execution evidence for SQLite but not complete evidence for configuration-driven persistence.
- **Recommendation:** Add a focused configuration/persistence test only where it can safely control the path and isolate the database; avoid coupling tests to the developer runtime database.
- **Priority:** P1
- **Status:** Partial

### F-011 — Runtime database isolation is verified by configuration, with a fixed-file parallelism risk

- **Category:** Isolation
- **Evidence:** Test configuration uses `./target/it-support-ticket-test.db`, disables demo-data startup, and the integration test no longer creates the runtime `./data` directory. The test class is transactional. The database filename is fixed rather than generated per test process.
- **Risk:** Concurrent Maven invocations in the same workspace could contend for the same SQLite file. This is a test-harness risk, not an observed production defect.
- **Impact:** Normal clean test runs are isolated from runtime data, but parallel local or CI executions could be less deterministic.
- **Recommendation:** Keep the current simple isolation for the existing execution model; consider a process-specific database only if parallel execution becomes a supported requirement.
- **Priority:** P2
- **Status:** Suspected

### F-012 — The application smoke test provides almost no requirement evidence

- **Category:** False confidence / Positive scenarios
- **Evidence:** `ItSupportTicketApplicationTest.smokeTest` only executes `assertTrue(true)`. It does not load the Spring context or exercise any documented behavior.
- **Risk:** The test can pass even if application startup or all business behavior is broken.
- **Impact:** The test count is higher without adding meaningful verification, which can mislead reviewers about suite strength.
- **Recommendation:** Replace it with a real context or behavior check only if an application-level test is required; otherwise do not count it as requirement coverage.
- **Priority:** P2
- **Status:** Verified

### F-013 — Maven configuration supports the current harness, but does not add test-level reporting

- **Category:** CI/test harness / Evidence
- **Evidence:** The root POM sets Java 22, the backend POM includes Spring Boot test support and SQLite, and the project runs through Maven Surefire. The tests run with JUnit 5, but the POM has no explicit coverage or test-report publication configuration.
- **Risk:** CI can enforce pass/fail while giving reviewers limited durable evidence beyond console logs and Surefire output.
- **Impact:** Automated verification exists, but evidence quality depends on CI log retention and generated reports.
- **Recommendation:** Treat Maven pass/fail as the current gate. Add reporting only as a separate CI/tooling decision; it is not required to validate the existing business behavior.
- **Priority:** P3
- **Status:** Verified

## Requirement Coverage Summary

| Requirement | Review conclusion | Status |
| --- | --- | --- |
| Requirement 1 — Ticket creation | Main positive and required-field rejection covered; persistence absence after rejection is not demonstrated. | Partial |
| Requirement 2 — Assignment | Positive, employee denial, non-technician target, and duplicate assignment covered; state matrix incomplete. | Partial |
| Requirement 3 — Work progression | Main positive path and blank-note/start-state failures covered; wrong identity and invalid resolution states are not tested. | Partial |
| Requirement 4 — Lifecycle status | Complete valid lifecycle covered; invalid transition coverage is representative, not exhaustive. | Partial |
| Requirement 5 — Role-based action control | Some role/header paths covered; missing/malformed headers and mismatched identity are not tested. | Partial |
| Requirement 6 — Validation and business errors | Many negative API paths covered; error fields are asserted inconsistently. | Partial |
| Requirement 7 — Persistence and configuration | SQLite test execution and isolation are evidenced; environment-driven path selection is not tested. | Partial |
| Requirement 8 — Demo data | Enabled-empty and disabled paths are unit-tested; seeded values and relationships are not verified. | Partial |

## Review Conclusion

The suite has credible API-level coverage for the main ticket workflow and uses real Spring/SQLite integration rather than controller mocks. The main false-confidence risks are concentrated in mock-count-only demo seeding, the no-op application smoke test, incomplete identity/transition negative paths, inconsistent error assertions, and lack of explicit configuration/durable-persistence verification. These are coverage and test-design findings, not claims of production defects.
