# Software requirements

## 1. Product overview

This project implements a lightweight internal IT support ticket workflow for local use. It supports a single ticket lifecycle from creation to assignment, work start, and resolution using a browser-based frontend and a Java 22 Spring Boot backend.

## 2. Scope

### In scope

- ticket creation by an employee
- ticket retrieval by ID
- assignment to one technician by a Tech Lead
- work start by the assigned technician
- ticket resolution with a required resolution note
- strict status transition validation
- SQLite persistence
- seeded demo data for a local demo environment

### Out of scope

- search/filter beyond direct ticket lookup by ID
- ticket comments or attachments
- multi-technician assignment
- external notifications
- external identity provider integration
- enterprise dashboards or analytics

## 3. Roles

The application currently supports these roles:

- EMPLOYEE
- TECH_LEAD
- IT_TECHNICIAN
- ADMIN

The frontend sets the user identity with the headers `X-User-Id` and `X-User-Role` for each backend call. The backend checks those values for authorization and action validation.

## 4. Ticket lifecycle

The implemented lifecycle is:

OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED

The backend rejects all transitions outside this flow. For example:

- OPEN cannot directly resolve
- ASSIGNED cannot resolve without first starting work
- RESOLVED is terminal in the current MVP

## 5. Functional requirements

### FR-001: ticket creation

An active requester and an active device are required to create a valid ticket. Title and description must be non-blank. If priority is omitted, the system defaults to MEDIUM.

### FR-002: ticket lookup

The system retrieves a ticket by ID and returns the current state, assignment, priority, and resolution details.

### FR-003: assignment

Only a Tech Lead can assign a ticket to a technician. The ticket must be OPEN and the technician must be an active IT technician.

### FR-004: work start

Only the assigned technician may begin work on an ASSIGNED ticket. The system updates the status to IN_PROGRESS.

### FR-005: resolution

Only the assigned technician may resolve an IN_PROGRESS ticket. A non-empty resolution note is required before the ticket is marked RESOLVED.

### FR-006: status validation

The backend validates transitions before persisting them. Invalid transitions return a structured business-rule error.

### FR-007: priority handling

Allowed priorities are LOW, MEDIUM, HIGH, URGENT. Missing priority defaults to MEDIUM.

### FR-008: SQLite persistence

The system stores ticket, device, and user data in SQLite. The database path is configurable through the environment variable `IT_SUPPORT_TICKET_DB_PATH`.

### FR-009: demo data

On an empty database and with demo mode enabled, the application seeds sample record sets automatically and does not duplicate them on subsequent runs.

### FR-010: structured validation errors

The backend returns structured error responses for validation, not-found, authorization, and rule-violation failures.

## 6. Non-functional requirements

The project is intentionally lightweight:

- local development focus
- no external infrastructure required
- file-based SQLite persistence
- simple Vite-based frontend
- clear backend ownership of business rules

## 7. Verification status

The current codebase includes backend tests covering the main ticket lifecycle and validation flows. The verified evidence includes 11 backend tests with 0 failures and 0 errors in the project Surefire reports.

## 8. Current implementation status

The current implementation covers the supported lifecycle and API contract implemented in the code. The repository documentation is intentionally written to describe the implemented behavior, not aspirational or planned features.

### FR-011: Security and role enforcement

- Requirement ID: FR-011
- Description: The application shall enforce role-based action control even without full enterprise identity integration.
- Preconditions: Application runtime has role metadata or a request identity abstraction.
- Input: Caller identity, role, and requested action.
- Processing/Business Rule: Access to ticket creation, assignment, and resolution is constrained by the actor’s role. The system shall not trust the client alone to enforce these checks.
- Output: Allowed action or structured denial response.
- Validation: Action checks occur on the server-side before any mutation.
- Error behavior: If the actor lacks the required role, deny the operation with a clear authorization/business error.
- Acceptance Criteria:
  - Given an employee identity, when the employee tries to assign a ticket, then the system denies the action.
  - Given a Tech Lead identity, when assignment is requested for an OPEN ticket, then the assignment succeeds.
  - Given a non-assigned technician identity, when work start is attempted, then the system denies the request.

### FR-012: API and UI contract

- Requirement ID: FR-012
- Description: The application shall expose minimal API operations for creating, viewing, assigning, starting, and resolving tickets and shall expose the ticket lifecycle in a user interface or equivalent response model.
- Preconditions: Application is running and connected to the configured database.
- Input: HTTP request payloads or equivalent UI actions.
- Processing/Business Rule: Minimal operations shall include ticket creation, retrieval, assignment, work start, and resolution. The status field shall remain visible to the user at all times.
- Output: JSON or equivalent response model containing the ticket state, assignment status, and any validation errors.
- Validation: APIs must validate required fields, transitions, and authorization checks.
- Error behavior: Return consistent HTTP or service-level errors for invalid requests and business-policy violations.
- Acceptance Criteria:
  - Given a valid ticket creation request, when the API is called, then it returns the created ticket with status OPEN.
  - Given a valid ticket ID, when the GET request is made, then the API returns the current status and assignment details.
  - Given an invalid update request, when the API is called, then the system returns a structured error response without changing the stored ticket.

## 8. Non-Functional Requirements

### NFR-001: Performance
The system shall complete normal create, view, assign, start, and resolve operations within 2 seconds in a local development environment with a small dataset. This requirement shall be evaluated with the local SQLite database and representative demo data.

### NFR-002: Reliability and atomicity
Each ticket state change shall be atomic; the system shall never partially update a ticket or leave it in an invalid status when a transition fails.

### NFR-003: Security and privacy
The application shall avoid logging sensitive ticket content beyond what is necessary for debugging. Role checks shall be enforced server-side. Full authentication is not required for the MVP, but authorization logic shall still be represented explicitly in the application design.

### NFR-004: Maintainability
State and validation rules shall be centralized in domain logic or a service layer rather than duplicated in UI code or controller logic.

### NFR-005: Usability
From the ticket view, a user shall understand the current status and the next valid action without specialist technical knowledge.

### NFR-006: Data portability and configuration management
The system shall support a configurable SQLite path and shall keep runtime database files out of Git. Database configuration must be external to source code and configurable by environment or application settings.

### NFR-007: Testing and quality
The application shall have automated tests covering the main lifecycle transitions, invalid transitions, role restrictions, validation failures, and demo-data initialization behavior.

### NFR-008: Build and release requirements
The project shall be buildable on a developer machine with a standard local runtime and SQLite support. Release readiness requires that the application can start with default demo data, run validation tests, and operate with a local file-based SQLite database without external services.

## 9. Search, Filter, and Categorization

### Search/Filter
The raw business requirement does not explicitly require searching or filtering tickets. Therefore, this feature is considered out of scope for the current MVP and shall not be treated as a required system behavior unless the business expands the requirement later.

### Category
The raw business requirement does not specify a ticket category field. Therefore, category is not required in the MVP. If a future requirement introduces category classification, it must be added separately with corresponding validation, persistence, and traceability updates.

## 10. Demo Data and Database Constraints

The following requirements are mandatory:

- SQLite shall be used as the persistence engine.
- SQLite is file-based and does not require a separate database server.
- Database path must be configurable.
- Runtime database files must not be committed to Git.
- The repository shall include Git ignore rules to prevent runtime database files from being tracked.
- Demo data shall be initialized automatically when the database is empty.
- Demo data initialization must be idempotent.
- Restarting the application must not create duplicate demo records.
- Demo data generation shall be disabled by configuration when needed.

## 11. Validation, Error Handling, and Persistence

### Validation
The system shall validate:

- required non-empty fields
- requester and equipment activity state
- ticket lifecycle transitions
- allowed roles for actions
- priority values
- non-empty resolution notes
- supported database configuration values

### Error handling
The application shall use stable, structured business errors with identifiers and human-readable messages. Errors must not silently update data or leave partial state.

### Persistence
Persistent ticket data shall include at least:

- ticket identifier
- requester identifier
- equipment identifier
- title
- description
- priority
- assigned technician identifier
- current status
- resolution note
- created timestamp
- updated timestamp

## 12. Testing Requirements

The following test categories are required for acceptance:

- Unit tests for validation logic and business status transitions
- Integration tests for SQLite persistence and initialization behavior
- API tests for create, view, assign, start, and resolve operations
- Negative tests for invalid transitions, duplicate assignment, and invalid role usage
- Regression tests ensuring demo data is not duplicated on restart
- Manual validation of the workflow using predefined demo records

## 13. Build and Release Requirements

The application shall support local build and execution without external services. The release process shall ensure:

- the database path is configured correctly
- the database file is not committed to version control
- demo data can be enabled or disabled via configuration
- the application starts successfully with a clean or empty database
- the lifecycle tests pass in the default environment

## 14. Ambiguities and Assumptions

### Ambiguities

- The exact ticket numbering or human-readable ticket code format is not specified.
- The exact user identity mechanism is unspecified; the repository does not yet define authentication or session management.
- The term “device” is used but the repository does not define whether all equipment must be tracked as a generic asset or a specific device class.
- The raw requirement does not specify whether ticket update history or audit logs must be persisted.
- The raw requirement does not define whether the admin role is a fixed role name or derived from a permission model.
- The business requirement does not mandate category classification, search/filter, or dashboard features.

### Assumptions

- The application is a local demo-grade MVP rather than a production enterprise deployment.
- Role checks are represented in application logic, even if user authentication is deferred.
- Ticket creation and assignment are performed through a simple UI or API without external integrations.
- SQLite is acceptable for the local MVP and is aligned with the database decision in this specification.
- Demo data is intended only for local demonstration and not for production data migration.

## 15. Requirement Summary

This specification translates the raw business requirement into a testable MVP that includes:

- Ticket creation and viewing
- Assignment to one technician
- Work start and resolution
- Strong validation of status transitions
- Persistent SQLite storage with configurable path
- Demo data automation and idempotent initialization
- Role enforcement and structured error handling
- Minimal API and frontend requirements for a local demo

The specification intentionally excludes features and behaviors not present in the original raw requirement unless explicitly stated as future work.
