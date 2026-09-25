# End-to-End Verification

## Source of Truth

Business requirements are taken only from `../01-requirements/Requirements-IT-Support-Ticket-Document.md`. API and implementation references are used to identify the exercised use case and evidence, but they do not add business requirements.

## Runtime Verification Context

- **Command:** `mvn clean test`
- **Runtime context:** Spring Boot integration test context with SQLite.
- **Test database:** `./target/it-support-ticket-test.db` from test configuration.
- **Demo startup data:** Disabled for integration tests.
- **Observed Maven result:** 20 tests, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **Scope limitation:** This is runtime verification through the application test context and `MockMvc`, not a deployed production-environment verification.

## Flow 1 — Ticket Creation

- **Requirement reference:** Requirement 1 — ticket creation.
- **API/use case:** Create ticket through `POST /api/tickets`; `TicketService.createTicket`.
- **Implementation:** `TicketController.createTicket` delegates to `TicketService.createTicket`; the service validates required fields and creates the ticket with `OPEN` status.
- **Preconditions:** Active employee and persisted device fixture are available.
- **Input:** Employee identity headers and ticket request containing device, title, and description.
- **Steps:** Submit a valid create request through `MockMvc`; assert the response status and ticket fields.
- **Expected result:** The request succeeds and the ticket is returned in `OPEN` state.
- **Actual result:** Valid create tests passed. Blank title and missing description tests returned structured 400 validation responses.
- **Automated test:** `createTicket_shouldReturnCreatedTicket`; `createTicket_shouldRejectBlankTitle`; `createTicket_shouldRejectMissingDescription`; `ticketLifecycle_shouldCreateAssignStartAndResolveTicket`.
- **Evidence:** Maven `BUILD SUCCESS`; `TicketControllerIntegrationTest`; isolated test SQLite configuration.
- **Status:** PASS

## Flow 2 — Ticket Viewing

- **Requirement reference:** In-scope behavior — view ticket by ID.
- **API/use case:** View ticket through `GET /api/tickets/{id}`; `TicketService.getTicketById`.
- **Implementation:** `TicketController.getTicket` delegates by ticket ID and returns the mapped ticket response.
- **Preconditions:** A persisted ticket exists.
- **Input:** Existing ticket ID and the headers used by the existing integration test.
- **Steps:** Request the ticket by ID; request a non-existent ID.
- **Expected result:** An existing ticket is returned; a missing ticket produces a not-found response.
- **Actual result:** Existing-ticket retrieval and missing-ticket retrieval tests passed. Authorization enforcement for viewing is addressed separately in Flow 5 and is not treated as verified here.
- **Automated test:** `getTicket_shouldReturnTicketById`; `getTicket_shouldReturnNotFound_whenMissing`.
- **Evidence:** Maven test result; `TicketControllerIntegrationTest`; `GlobalExceptionHandler` not-found mapping.
- **Status:** PASS

## Flow 3 — Technician Assignment

- **Requirement reference:** Requirement 2 — assignment.
- **API/use case:** Assign technician through `POST /api/tickets/{id}/assign`; `TicketService.assignTechnician`.
- **Implementation:** The service requires the Tech Lead role, checks `OPEN` status, rejects an existing assignment, and requires an IT technician target.
- **Preconditions:** An `OPEN` ticket, active Tech Lead, and active IT technician exist.
- **Input:** Tech Lead identity headers and technician user ID.
- **Steps:** Submit a valid assignment; attempt assignment by an employee; attempt assignment to a non-technician; attempt duplicate assignment.
- **Expected result:** Valid assignment returns `ASSIGNED` with one technician; rejected operations return authorization/business errors and preserve the ticket state.
- **Actual result:** Valid assignment, employee rejection, non-technician rejection, and duplicate assignment tests passed.
- **Automated test:** `assignTicket_shouldMoveToAssigned`; `assignTicket_shouldRejectEmployeeAndPreserveOpenState`; `assignTicket_shouldRejectNonTechnicianAssigneeAndPreserveOpenState`; `assignTicket_shouldRejectDuplicateAssignment`.
- **Evidence:** Maven test result; API response assertions in `TicketControllerIntegrationTest`; service role/state checks.
- **Status:** PASS

## Flow 4 — Work Progression and Resolution

- **Requirement reference:** Requirement 3 — work progression.
- **API/use case:** Start work through `POST /api/tickets/{id}/start`; resolve through `POST /api/tickets/{id}/resolve`.
- **Implementation:** `TicketService.startWork` requires an assigned technician and `ASSIGNED` status. `TicketService.resolveTicket` requires the assigned technician, `IN_PROGRESS` status, and a non-blank resolution note.
- **Preconditions:** An assigned technician and ticket in the required state exist.
- **Input:** Technician identity headers and, for resolution, a resolution note.
- **Steps:** Start work; resolve with a valid note; attempt start from `OPEN`; attempt resolution with a blank note.
- **Expected result:** Start returns `IN_PROGRESS`; valid resolution returns `RESOLVED` with the note; invalid operations return structured errors without an invalid state change.
- **Actual result:** Positive start/resolution, invalid `OPEN` start, and blank-note rejection tests passed.
- **Automated test:** `startWork_shouldUpdateStatusToInProgress`; `startWork_shouldRejectOpenTicketAndPreserveState`; `resolveTicket_shouldUpdateStatusToResolved`; `resolveTicket_shouldRejectBlankResolutionNote`.
- **Evidence:** Maven test result; API assertions and unchanged-state assertions in `TicketControllerIntegrationTest`.
- **Status:** PASS

## Flow 5 — Complete Lifecycle and Role-Based Control

- **Requirement reference:** Requirement 4 — lifecycle status; Requirement 5 — role-based action control.
- **API/use case:** Create, assign, start, resolve, and view the ticket through the API.
- **Implementation:** `TicketStateMachine` defines `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED`; state-changing controller methods parse role headers and delegate actor checks to `TicketService`.
- **Preconditions:** Employee requester, Tech Lead, technician, and active device are available.
- **Input:** Requests with `X-User-Id` and `X-User-Role` headers.
- **Steps:** Execute the complete lifecycle; inspect the view endpoint authorization boundary.
- **Expected result:** The lifecycle succeeds in order, invalid transitions are rejected, and every backend action validates the caller context.
- **Actual result:** The lifecycle integration test and representative invalid-transition tests passed. However, `TicketController.getTicket` accepts only the path ID and does not receive or validate identity headers; `TicketService.getTicketById` performs no actor/role authorization check. This does not satisfy the documented requirement that ticket viewing requires caller identity and an allowed role.
- **Automated test:** `ticketLifecycle_shouldCreateAssignStartAndResolveTicket`; `startWork_shouldRejectOpenTicketAndPreserveState`; `assignTicket_shouldRejectDuplicateAssignment`.
- **Evidence:** `TicketController.java`, `TicketService.java`, `../03-api/api-spec.md` API-02, and `../06-quality-security/owasp-review.md` SEC-001. The existing GET test supplies headers but does not prove that they affect authorization.
- **Status:** FAIL

### Defect Detected — Verification Stopped

The view authorization mismatch is a defect against the documented API behavior. Verification stops here for RCA as required. No production code was changed during this verification.

- **Defect:** `GET /api/tickets/{id}` does not enforce the required caller identity/role context.
- **Observed evidence:** The controller method has no identity-header parameters and the service method has no actor authorization input.
- **Security impact:** A caller who knows a ticket ID may receive ticket details without the documented authorization check.
- **RCA handoff:** Define the intended view policy, then update implementation and add unauthorized-view verification before declaring the end-to-end path complete.

## Flow 6 — Validation and Structured Errors

- **Requirement reference:** Requirement 6 — validation and business errors.
- **API/use case:** Create, assign, start, resolve, and malformed-request error handling.
- **Implementation:** `TicketService` raises validation/business exceptions; `GlobalExceptionHandler` maps known exceptions to HTTP responses.
- **Preconditions:** Invalid input or invalid workflow action is prepared.
- **Input:** Blank title, missing description, invalid priority, blank resolution note, duplicate assignment, invalid start state, or malformed JSON.
- **Steps:** Submit representative invalid requests and inspect status, error code, message, and path.
- **Expected result:** Structured HTTP errors are returned and failed state-changing operations do not mutate ticket state.
- **Actual result:** Existing tests cover representative cases, but this flow was not independently continued after the Flow 5 defect was detected. Error-field assertions are not uniform for every negative case.
- **Automated test:** Existing negative tests in `TicketControllerIntegrationTest`, including `createTicket_shouldRejectBlankTitle`, `createTicket_shouldRejectMissingDescription`, `createTicket_shouldRejectInvalidPriority`, `resolveTicket_shouldRejectBlankResolutionNote`, `malformedRequest_shouldReturnBadRequest`, and assignment/start rejection tests.
- **Evidence:** Maven test run passed before the verification stop; `GlobalExceptionHandler`; `../04-design/test-design.md` and `../06-quality-security/ai-test-review.md`.
- **Status:** NOT VERIFIED

## Flow 7 — SQLite Persistence and Configuration

- **Requirement reference:** Requirement 7 — persistence and configuration.
- **API/use case:** Repository-backed ticket operations using SQLite.
- **Implementation:** Spring datasource configuration uses SQLite; test configuration selects `./target/it-support-ticket-test.db`.
- **Preconditions:** Maven test environment and SQLite JDBC dependency are available.
- **Input:** Repository-backed integration requests and configured test datasource.
- **Steps:** Start the Spring test context; create/read/update ticket data through the integration tests; inspect the configured test database path.
- **Expected result:** SQLite operations work and `IT_SUPPORT_TICKET_DB_PATH` can select an alternate database location.
- **Actual result:** SQLite-backed integration tests started and passed using the isolated test database. The environment-variable path-selection behavior was not executed as a separate verification scenario.
- **Automated test:** `TicketControllerIntegrationTest` repository-backed integration tests.
- **Evidence:** Maven test output; `src/test/resources/application.yml`; runtime `application.yml`; backend POM SQLite dependency.
- **Status:** NOT VERIFIED

## Flow 8 — Demo Data Initialization

- **Requirement reference:** Requirement 8 — demo data.
- **API/use case:** `DemoDataInitializer.run` during application startup.
- **Implementation:** The initializer checks the enabled flag and repository counts, then saves sample users, devices, and tickets when the database is empty.
- **Preconditions:** Empty database and demo-data enabled.
- **Input:** Application startup with demo configuration.
- **Steps:** Start the application with empty SQLite storage and observe seeded users, devices, and tickets.
- **Expected result:** Sample records are automatically seeded.
- **Actual result:** The current automated test uses Mockito repositories and verifies save interactions when enabled, plus no saves when disabled. It does not perform runtime startup against an empty database or verify seeded record contents.
- **Automated test:** `DemoDataInitializerTest.run_shouldSeedDemoData_whenEnabledAndEmpty`; `DemoDataInitializerTest.run_shouldSkipSeeding_whenDemoDataDisabled`.
- **Evidence:** `DemoDataInitializer.java`, `DemoDataInitializerTest.java`, test configuration with demo data disabled.
- **Status:** NOT VERIFIED

## Verification Summary

- **Runtime command:** `mvn clean test`
- **Observed result:** 20 tests passed, 0 failures, 0 errors, 0 skipped; `BUILD SUCCESS`.
- **Verified flows before stop:** Ticket creation, ticket retrieval behavior, assignment, work progression/resolution, and the valid lifecycle path.
- **Defect requiring RCA:** Ticket viewing does not enforce the documented identity/authorization context.
- **Not verified after stop:** Full error-contract completion, environment-driven database-path selection, and runtime demo-data seeding.
