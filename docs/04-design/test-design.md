# Test Design from Specification

## Scope and Source

This document traces the requirements in `../01-requirements/Requirements-IT-Support-Ticket-Document.md` to positive, negative, and boundary test scenarios. The source document defines eight requirements and does not assign separate FR or API identifiers, so this document uses `Requirement 1` through `Requirement 8` as the requirement references.

The existing automated-test references below are limited to test classes and methods currently present in the repository.

## Requirement 1 — Ticket Creation

**Requirement summary:** An employee may create a support request for a device issue. The request includes the affected device, a short title, and a detailed description. A created ticket starts in `OPEN`.

**Test design:**

- **Positive scenario:** An employee creates a ticket with the affected device, title, and description.
- **Negative scenario:** Reject a ticket with a blank title or missing description.
- **Boundary/edge scenario:** Verify that the created ticket starts in `OPEN` and that required fields are not accepted as blank values.
- **Expected result:** A valid request succeeds and returns a ticket in `OPEN`; invalid required fields return a structured validation error and do not create a valid ticket.
- **Test classification:** Integration, API, Validation.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.createTicket_shouldReturnCreatedTicket`
  - `TicketControllerIntegrationTest.createTicket_shouldRejectBlankTitle`
  - `TicketControllerIntegrationTest.createTicket_shouldRejectMissingDescription`
  - `TicketControllerIntegrationTest.ticketLifecycle_shouldCreateAssignStartAndResolveTicket`
- **Additional existing coverage:** `TicketControllerIntegrationTest.createTicket_shouldDefaultPriorityToMedium_whenPriorityIsMissing` covers an implementation behavior not specified by the source requirement.
- **Gap:** The existing tests do not explicitly verify the persistence absence of a rejected create request.

## Requirement 2 — Assignment

**Requirement summary:** A Tech Lead may assign a ticket to a single IT technician, and only one technician may be assigned at a time.

**Test design:**

- **Positive scenario:** A Tech Lead assigns an IT technician to an unassigned ticket.
- **Negative scenario:** Reject assignment by an employee, assignment of a non-technician user, and duplicate assignment.
- **Boundary/edge scenario:** Attempt assignment after the ticket already has an assignment; the ticket must remain unchanged.
- **Expected result:** Valid assignment succeeds and records one technician; invalid or duplicate assignment returns a business/authorization error without changing the ticket state.
- **Test classification:** Integration, API, Authorization, State transition, Validation.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.assignTicket_shouldMoveToAssigned`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectEmployeeAndPreserveOpenState`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectNonTechnicianAssigneeAndPreserveOpenState`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectDuplicateAssignment`
  - `TicketControllerIntegrationTest.ticketLifecycle_shouldCreateAssignStartAndResolveTicket`
- **Gap:** Assignment to a ticket in another invalid status is only indirectly represented by the duplicate assignment scenario.

## Requirement 3 — Work Progression

**Requirement summary:** The assigned technician may begin work, moving a ticket from `ASSIGNED` to `IN_PROGRESS`; resolution is allowed only when the ticket is `IN_PROGRESS`.

**Test design:**

- **Positive scenario:** The assigned technician starts an `ASSIGNED` ticket and then resolves an `IN_PROGRESS` ticket with a resolution note.
- **Negative scenario:** Reject starting work while the ticket is `OPEN`; reject resolution with a blank note.
- **Boundary/edge scenario:** Verify the resolution note is required when resolution is attempted.
- **Expected result:** Valid actions return the next expected state; invalid actions return structured business or validation errors and preserve the prior state.
- **Test classification:** Integration, API, Authorization, Validation, State transition.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.startWork_shouldUpdateStatusToInProgress`
  - `TicketControllerIntegrationTest.startWork_shouldRejectOpenTicketAndPreserveState`
  - `TicketControllerIntegrationTest.resolveTicket_shouldUpdateStatusToResolved`
  - `TicketControllerIntegrationTest.resolveTicket_shouldRejectBlankResolutionNote`
  - `TicketControllerIntegrationTest.ticketLifecycle_shouldCreateAssignStartAndResolveTicket`
- **Gap:** A non-assigned technician attempting to start or resolve work is not directly covered. Resolution attempts from `OPEN` or `ASSIGNED` are not directly covered.

## Requirement 4 — Lifecycle Status

**Requirement summary:** The supported lifecycle is `OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED`; invalid transitions are rejected.

**Test design:**

- **Positive scenario:** Execute the complete supported lifecycle through the API.
- **Negative scenario:** Attempt to start work from `OPEN` and attempt assignment after the ticket is already `ASSIGNED`.
- **Boundary/edge scenario:** Verify the final response remains `RESOLVED` after the complete lifecycle.
- **Expected result:** Only the documented sequence succeeds; invalid transitions return a business-rule error and do not change the ticket's current state.
- **Test classification:** Integration, API, State transition.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.ticketLifecycle_shouldCreateAssignStartAndResolveTicket`
  - `TicketControllerIntegrationTest.startWork_shouldRejectOpenTicketAndPreserveState`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectDuplicateAssignment`
- **Gap:** There is no direct test for an update attempt after `RESOLVED`, and no direct coverage of every invalid transition pair.

## Requirement 5 — Role-Based Action Control

**Requirement summary:** The backend validates acting-user role and identity. The frontend supplies `X-User-Id` and `X-User-Role` headers for backend authorization.

**Test design:**

- **Positive scenario:** A Tech Lead assigns a technician, and the assigned technician starts and resolves the ticket using the identity and role headers.
- **Negative scenario:** An employee attempts assignment and is rejected.
- **Boundary/edge scenario:** The acting identity must correspond to the assigned technician for work progression; this scenario is defined by the requirement but is not currently automated.
- **Expected result:** Authorized role/identity combinations succeed; unauthorized combinations return an authorization error before the ticket is changed.
- **Test classification:** Integration, API, Authorization.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.assignTicket_shouldMoveToAssigned`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectEmployeeAndPreserveOpenState`
  - `TicketControllerIntegrationTest.startWork_shouldUpdateStatusToInProgress`
  - `TicketControllerIntegrationTest.ticketLifecycle_shouldCreateAssignStartAndResolveTicket`
- **Gap:** Missing headers and a different technician identity are not directly covered. The existing `GET` test supplies headers, but the requirement's authorization behavior is not independently asserted for ticket viewing.

## Requirement 6 — Validation and Business Errors

**Requirement summary:** Invalid input, blank notes, duplicate assignment, and other business-rule violations are rejected with structured HTTP error responses.

**Test design:**

- **Positive scenario:** Valid create, assignment, start, and resolution requests return successful API responses.
- **Negative scenario:** Invalid input, blank resolution note, employee assignment, non-technician assignment, invalid start state, and duplicate assignment are rejected.
- **Boundary/edge scenario:** Representative errors must expose the response status, error code, message, and path fields.
- **Expected result:** Invalid requests return structured errors; valid requests return successful ticket responses; rejected workflow operations preserve state where the existing tests assert it.
- **Test classification:** Integration, API, Validation, Authorization, State transition.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest.createTicket_shouldRejectBlankTitle`
  - `TicketControllerIntegrationTest.createTicket_shouldRejectMissingDescription`
  - `TicketControllerIntegrationTest.createTicket_shouldRejectInvalidPriority`
  - `TicketControllerIntegrationTest.resolveTicket_shouldRejectBlankResolutionNote`
  - `TicketControllerIntegrationTest.malformedRequest_shouldReturnBadRequest`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectEmployeeAndPreserveOpenState`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectNonTechnicianAssigneeAndPreserveOpenState`
  - `TicketControllerIntegrationTest.startWork_shouldRejectOpenTicketAndPreserveState`
  - `TicketControllerIntegrationTest.assignTicket_shouldRejectDuplicateAssignment`
- **Additional existing coverage:** `TicketControllerIntegrationTest.createTicket_shouldRejectInvalidPriority` covers an implementation validation behavior not explicitly named by the source requirement.
- **Gap:** Structured error fields are not asserted consistently for every existing negative case. Missing identity and malformed header cases are not covered.

## Requirement 7 — Persistence and Configuration

**Requirement summary:** The system stores data in SQLite and supports database-path configuration through `IT_SUPPORT_TICKET_DB_PATH`.

**Test design:**

- **Positive scenario:** The integration test starts with SQLite and persists the entities needed to exercise ticket API behavior.
- **Negative scenario:** The source requirement does not define a failure behavior for an unusable configured database path.
- **Boundary/edge scenario:** Change the `IT_SUPPORT_TICKET_DB_PATH` value and verify that the application uses the configured SQLite location.
- **Expected result:** Test execution uses SQLite successfully, and a configured `IT_SUPPORT_TICKET_DB_PATH` selects the configured database location.
- **Test classification:** Integration, Persistence.
- **Existing automated tests:**
  - `TicketControllerIntegrationTest` uses the configured SQLite test database while exercising repository-backed API behavior.
- **Gap:** There is no automated test proving that `IT_SUPPORT_TICKET_DB_PATH` changes the selected database path.

## Requirement 8 — Demo Data

**Requirement summary:** When the database is empty and demo data is enabled, sample users, devices, and tickets are seeded automatically.

**Test design:**

- **Positive scenario:** Run the initializer with demo data enabled and empty repositories; verify that seed saves occur.
- **Negative scenario:** Run with demo data disabled and verify that no seed saves occur.
- **Boundary/edge scenario:** Run with an empty database and demo data enabled; the initializer should seed the sample data once for that startup.
- **Expected result:** Empty enabled storage is seeded and disabled mode does not seed.
- **Test classification:** Unit, Persistence.
- **Existing automated tests:**
  - `DemoDataInitializerTest.run_shouldSeedDemoData_whenEnabledAndEmpty`
  - `DemoDataInitializerTest.run_shouldSkipSeeding_whenDemoDataDisabled`
- **Gap:** The current source requirement does not define non-empty-database or repeated-startup behavior. The existing tests also do not assert the content of seeded records and relationships.

## Existing Test Reverse Traceability

| Automated test class | Current requirement references |
| --- | --- |
| `TicketControllerIntegrationTest` | Requirements 1, 2, 3, 4, 5, 6, and partial Requirement 7 |
| `DemoDataInitializerTest` | Requirement 8, partially |
| `ItSupportTicketApplicationTest.smokeTest` | No behavior-specific requirement; only a basic assertion |

## Test Coverage Gaps

- Requirement 1: persistence absence after rejected creation.
- Requirement 2: broader invalid-status assignment cases.
- Requirement 3: wrong technician identity; resolution from invalid prior states.
- Requirement 4: attempts to modify a `RESOLVED` ticket and complete invalid-transition matrix.
- Requirement 5: missing/invalid identity headers and mismatched technician identity.
- Requirement 6: consistent structured-error assertions across all negative API cases.
- Requirement 7: environment-variable path selection.
- Requirement 8: verification of seeded record content and relationships.
- `ItSupportTicketApplicationTest.smokeTest` does not trace to a behavior-specific requirement because it only asserts `true`.
