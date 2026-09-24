# Software Requirements Specification (SRS)

- Document status: Draft for review
- Version: 2.0
- Date: 2026-09-24
- Product: AI-native IT Support Ticket Application
- Domain: Internal IT support and service request management

## 1. Purpose

This document converts the raw business requirement for an internal IT support ticket application into a structured, testable software requirements specification. It preserves the original business intent from the repository, explicitly calls out assumptions and ambiguities, and defines a clear MVP boundary for implementation.

The requirement set is intentionally constrained to the business intent present in the repository: employees create support tickets, Tech Leads assign them, IT technicians update status and resolve work, and administrators manage the system data and demo environment. Nothing in this specification adds enterprise features that are not present in the raw business requirement unless explicitly marked as future work.

## 2. Product Context and Business Objective

The application provides a lightweight, internal ticket workflow for managing equipment-related IT incidents. The workflow shall support a single ticket lifecycle from creation through assignment, work start, and resolution. The system shall be demonstrable in a local development environment using SQLite and sample data.

## 3. Scope

### 3.1 In Scope

- Ticket creation by an employee for an equipment issue
- Ticket viewing by ID and status
- Assignment of one open ticket to one IT technician
- Technician workflow actions: start work and resolve ticket
- Status lifecycle management: OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED
- Validation of required fields and business rules
- Structured error handling
- Persistence in SQLite and configurable DB path
- Demo/sample initialization when the database is empty
- Role-based action checks without requiring enterprise identity management
- Minimal API and frontend behaviors needed for a working local demo

### 3.2 Out of Scope

The following items are explicitly out of scope for the current MVP and shall not be implemented unless the business requirement is expanded later:

- Email, SMS, chat, or push notifications
- File attachments or screenshots
- SLA timer automation and escalation
- Ticket comments, activity feeds, or threaded discussion
- Approval workflows and workflow orchestration
- Asset inventory, procurement, or lifecycle tracking
- Knowledge base or self-service articles
- Multiple technicians assigned to a single ticket
- Enterprise single sign-on or external identity provider integration
- Analytics dashboards or executive reporting
- Search/filter beyond the minimum listing or lookup requirement, if not explicitly required by the raw requirement
- Category management, because no category requirement was present in the raw business requirement
- External system integrations with external service management tools

## 4. Roles and Actors

| Role | Description | Allowed actions |
| --- | --- | --- |
| Employee | Internal user who reports an equipment issue | Create ticket, view own ticket status |
| Tech Lead | Internal user who assigns tickets to technicians | Assign ticket to one technician |
| IT Technician | Internal user who handles assigned tickets | Start work, update status, resolve ticket |
| Administrator | Internal user who validates the demo and supports system administration | View all tickets, manage demo data and configuration |

## 5. Ticket Lifecycle

The supported lifecycle is:

OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED

Transition rules:

- OPEN -> ASSIGNED: performed only by a Tech Lead for an unassigned ticket
- ASSIGNED -> IN_PROGRESS: performed only by the assigned IT technician
- IN_PROGRESS -> RESOLVED: performed only by the assigned IT technician with a non-empty resolution note
- Any other transition is invalid and rejected
- A resolved ticket is treated as terminal in this MVP

## 6. Business Requirements Overview

### BR-001: Ticket creation requirement
A ticket must include device information, a title, and a description, and the requester must be active.

### BR-002: Validation requirement
A ticket record may not be created with blank required fields or with an inactive requester or inactive device.

### BR-003: Assignment requirement
Only a Tech Lead can assign a ticket to one active IT technician, and only when the ticket is OPEN.

### BR-004: Assignment uniqueness requirement
A single ticket may have at most one active assigned technician at a time.

### BR-005: Work start requirement
Only the assigned IT technician may change the ticket status from ASSIGNED to IN_PROGRESS.

### BR-006: Resolution requirement
Only an IN_PROGRESS ticket may be resolved, and a non-empty resolution note is mandatory.

### BR-007: Priority requirement
If no priority is provided by the requester, the system shall default to MEDIUM.

### BR-008: Status integrity requirement
The system shall reject invalid status transitions and preserve the last valid ticket state.

### BR-009: Database configuration requirement
The application shall use SQLite, the database path shall be configurable, and runtime database files shall not be committed to Git.

### BR-010: Demo data requirement
If the database is empty, predefined demo data shall be initialized automatically; initialization must be idempotent and configurable for disablement.

## 7. Functional Requirements

### FR-001: Ticket creation

- Requirement ID: FR-001
- Description: The system shall allow an active employee to create a support ticket for an active device or equipment item.
- Preconditions: The requester exists and is active; the device exists and is active; the caller is authorized to create a request in the current role context.
- Input: Requester ID, device ID, title, description, optional priority.
- Processing/Business Rule: The system validates all required fields. If priority is missing, default it to MEDIUM. The ticket is created in status OPEN.
- Output: Created ticket record including generated ID, timestamps, current status, and default priority.
- Validation: Title and description must be non-blank after trimming whitespace; requester and device must be active; requester must be allowed to create tickets in this role context.
- Error behavior: Return a structured validation error if the requester is inactive, device is invalid, or required fields are missing.
- Acceptance Criteria:
  - Given an active employee and active device, when the employee submits a valid ticket request, then a new ticket is created with status OPEN.
  - Given a missing title, when the employee submits the request, then the ticket is rejected with a validation error.
  - Given a missing description, when the employee submits the request, then the ticket is rejected with a validation error.
  - Given a valid request without priority, when the ticket is created, then the stored priority equals MEDIUM.

### FR-002: Ticket viewing

- Requirement ID: FR-002
- Description: The system shall allow authorized users to retrieve a ticket by its identifier and inspect its current state.
- Preconditions: Ticket exists; caller is allowed to view tickets in the current role context.
- Input: Ticket ID.
- Processing/Business Rule: The system retrieves the ticket and includes status, requester, assigned technician, equipment, priority, and resolution note if present.
- Output: Ticket details object with status and assignment data.
- Validation: Ticket ID must exist and be valid.
- Error behavior: Return not-found or unauthorized error if the ticket does not exist or the caller is not authorized.
- Acceptance Criteria:
  - Given a valid ticket ID, when the user requests ticket details, then the response returns the current status, requester, and equipment information.
  - Given a non-existent ticket ID, when the user requests the ticket, then the system returns a stable not-found error.
  - Given an employee requests a ticket that is not accessible under the current policy, when the request is made, then access is denied with a domain-level error.

### FR-003: Ticket assignment

- Requirement ID: FR-003
- Description: The system shall allow a Tech Lead to assign one active IT technician to an OPEN ticket.
- Preconditions: Ticket exists and is in OPEN state; Tech Lead role is active; technician exists and is active; technician is an IT technician.
- Input: Ticket ID, technician ID, actor role.
- Processing/Business Rule: The system verifies role and state; if an assignment is already active, reject the request; otherwise assign the technician and update status to ASSIGNED.
- Output: Updated ticket record with new assigned technician and status transition information.
- Validation: Only one technician can be assigned, and assignment is only allowed for OPEN tickets.
- Error behavior: If the ticket is not OPEN, the technician is inactive, or the actor is not a Tech Lead, return a structured business error and leave the ticket unchanged.
- Acceptance Criteria:
  - Given an OPEN ticket and an active Tech Lead, when a valid assignment request is submitted, then the ticket status becomes ASSIGNED.
  - Given an already assigned ticket, when a duplicate assignment request is submitted, then the assignment is rejected without modifying the ticket.
  - Given a non-Tech Lead caller, when assignment is attempted, then the attempt is rejected with an authorization/business error.

### FR-004: Work start

- Requirement ID: FR-004
- Description: The system shall allow the assigned IT technician to start work on an ASSIGNED ticket.
- Preconditions: Ticket exists; status is ASSIGNED; caller is the assigned technician.
- Input: Ticket ID, actor identity.
- Processing/Business Rule: The system checks that the caller is the assigned technician and that the ticket state is ASSIGNED. Then it changes the ticket status to IN_PROGRESS.
- Output: Updated ticket record with status IN_PROGRESS and updated timestamp.
- Validation: Only the assigned technician may start work.
- Error behavior: If a different technician attempts to start work, the request is rejected and the ticket state remains unchanged.
- Acceptance Criteria:
  - Given the assigned technician for an ASSIGNED ticket, when start-work is triggered, then the status changes to IN_PROGRESS.
  - Given another technician attempts to start the same ticket, when the request is made, then it is rejected and status remains ASSIGNED.
  - Given a ticket already in IN_PROGRESS, when start-work is attempted, then the request is rejected with an invalid transition error.

### FR-005: Ticket resolution

- Requirement ID: FR-005
- Description: The system shall allow the assigned IT technician to resolve an IN_PROGRESS ticket with a non-empty resolution note.
- Preconditions: Ticket exists; ticket is IN_PROGRESS; caller is the assigned technician.
- Input: Ticket ID, resolution note, actor identity.
- Processing/Business Rule: The system validates non-empty note content and verifies ticket is in IN_PROGRESS. If valid, it updates status to RESOLVED and stores the resolution note with timestamp.
- Output: Resolved ticket record with final status and resolution note.
- Validation: Resolution note must contain meaningful content after trimming whitespace.
- Error behavior: If the note is blank or the caller is not the assigned technician, the ticket remains unchanged and a structured error is returned.
- Acceptance Criteria:
  - Given an IN_PROGRESS ticket and the assigned technician, when a valid resolution note is submitted, then the ticket status changes to RESOLVED.
  - Given an IN_PROGRESS ticket and a blank resolution note, when resolution is attempted, then the system returns a validation error and does not resolve the ticket.
  - Given a resolved ticket, when another resolution attempt is made, then the system rejects the operation as an invalid transition.

### FR-006: Status transition validation

- Requirement ID: FR-006
- Description: The system shall enforce the supported ticket lifecycle and reject invalid transitions.
- Preconditions: Ticket exists and has a current status.
- Input: Current status, requested new status, actor identity, and action type.
- Processing/Business Rule: Only the legal transitions OPEN->ASSIGNED, ASSIGNED->IN_PROGRESS, and IN_PROGRESS->RESOLVED are allowed. All other transitions are invalid.
- Output: Either a success update or a structured validation error with unchanged state.
- Validation: Transition must be checked against the business status model before persistence.
- Error behavior: Reject invalid transitions with a stable error code and human-readable explanation without partially updating the record.
- Acceptance Criteria:
  - Given an OPEN ticket, when the system tries to move it directly to RESOLVED, then the request is rejected.
  - Given an ASSIGNED ticket, when a Tech Lead attempts to change it to OPEN, then the request is rejected.
  - Given a RESOLVED ticket, when any update is attempted, then the system rejects the event and preserves the resolved state.

### FR-007: Priority handling

- Requirement ID: FR-007
- Description: The system shall record ticket priority and apply a default value when the requester supplies none.
- Preconditions: Ticket creation request is valid.
- Input: Priority value, if provided.
- Processing/Business Rule: Valid values are LOW, MEDIUM, HIGH, and URGENT. If omitted or empty, default to MEDIUM.
- Output: Persistent ticket priority field.
- Validation: If a non-supported value is submitted, reject with validation error.
- Error behavior: Return a business validation error and do not create the ticket.
- Acceptance Criteria:
  - Given a valid ticket request without a priority value, when the ticket is created, then the stored priority is MEDIUM.
  - Given a request with priority HIGH, when the ticket is created, then the stored priority is HIGH.
  - Given an unsupported priority value, when the ticket is submitted, then the system rejects the request.

### FR-008: Persistence and SQLite configuration

- Requirement ID: FR-008
- Description: The application shall persist ticket data to SQLite using a configurable database path.
- Preconditions: SQLite library and application runtime environment are available.
- Input: Database configuration including file path and optional file name.
- Processing/Business Rule: The system shall store all ticket and reference data in a SQLite database file whose location is configurable. Runtime database files shall not be committed to Git.
- Output: Persistent storage of ticket, status, assignment, and resolution data.
- Validation: Database connection must be created using the configured path; invalid paths must be surfaced as configuration errors.
- Error behavior: If the database cannot be opened or the configured path is invalid, the application must fail gracefully with a clear error and no partial writes.
- Acceptance Criteria:
  - Given configuration specifies a valid SQLite file path, when the application starts, then it connects successfully to that database.
  - Given the database path is changed in configuration, when the application restarts, then it uses the new path for persistence.
  - Given a runtime SQLite database file is present in the repository, when the project is reviewed, then the file is excluded from Git by configuration rules.

### FR-009: Demo data initialization

- Requirement ID: FR-009
- Description: The application shall initialize predefined demo ticket data automatically when the database is empty.
- Preconditions: Database exists or is newly created; demo data flag is enabled.
- Input: Empty database state and demo configuration.
- Processing/Business Rule: On startup, if the database is empty, the system inserts the predefined demo data set once. Initialization must be idempotent and must not duplicate records on restart. If demo data is disabled in configuration, no demo records shall be inserted.
- Output: Demo user, equipment, and ticket records available for local demonstration.
- Validation: Check whether the database is empty before seeding; verify uniqueness constraints before insert; skip inserts if demo data already exists.
- Error behavior: If seeding fails due to a data conflict or configuration issue, log a clear startup error and continue with the current database state without creating duplicate records.
- Acceptance Criteria:
  - Given a newly created database and demo mode enabled, when the application starts, then demo records are created once.
  - Given the application restarts with the same database and demo mode enabled, when startup completes, then no duplicate demo records are created.
  - Given demo mode is disabled, when the application starts, then no demo records are inserted.

### FR-010: Validation and structured error handling

- Requirement ID: FR-010
- Description: The application shall validate required fields and return stable, structured business errors for invalid operations.
- Preconditions: Any operation that creates, updates, or resolves a ticket.
- Input: User action plus required data payload.
- Processing/Business Rule: The system validates required fields, role checks, status transitions, and data integrity before updating disk state. Validation failures produce stable error codes and messages.
- Output: Structured response containing error code, message, affected entity ID, and action context when applicable.
- Validation: Error handling is mandatory for invalid transitions, blank notes, inactive actors, unknown devices, duplicate assignment, or malformed input.
- Error behavior: No partial data mutation is allowed on failed validation or business-action errors.
- Acceptance Criteria:
  - Given a blank title or description, when the ticket is created, then the system returns a validation error code and no ticket is stored.
  - Given a duplicate assignment, when the assignment API is called, then the system returns a stable business error and preserves the existing ticket state.
  - Given a request that violates workflow rules, when the action is attempted, then the system returns a structured response and leaves the underlying data unchanged.

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
