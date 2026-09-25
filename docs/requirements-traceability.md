# Requirements Traceability Matrix

## 1. Purpose

This document maps the original raw business requirement to the derived software requirements, implementation components, APIs, and validation tests. It ensures that each business need is traceable through the software specification and can be tested.

## 2. Traceability Matrix

| Raw Requirement | Business Intent | Software Requirement ID(s) | Component | API / Interface | Test Case |
| --- | --- | --- | --- | --- | --- |
| RQ-01 | Employees submit support requests for equipment issues with required context and can check current status | FR-001, FR-002, FR-007, FR-010, BR-001, BR-002, BR-007 | TicketService, TicketValidator, TicketController | POST /api/tickets, GET /api/tickets/{id} | TC-001, TC-002, TC-003 |
| RQ-02 | Tech Lead reviews tickets and assigns them to a single responsible IT technician | FR-003, FR-006, FR-011, BR-003, BR-004 | AssignmentService, TicketStateMachine | POST /api/tickets/{id}/assign | TC-004, TC-005, TC-006 |
| RQ-03 | IT technician begins work, updates status, and records the resolution result | FR-004, FR-005, FR-006, FR-011, BR-005, BR-006, BR-008 | WorkProgressService, ResolutionService, TicketStateMachine | POST /api/tickets/{id}/start, POST /api/tickets/{id}/resolve | TC-007, TC-008, TC-009, TC-010 |
| RQ-04 | Ticket lifecycle is OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED | FR-006, BR-008, BR-006 | TicketStateMachine, DomainModel | Internal lifecycle logic, API transition checks | TC-011, TC-012, TC-013 |
| RQ-05 | Admin role manages supporting data and inspects system state | FR-002, FR-011, FR-012, NFR-005 | AdminService, TicketController, UI/Controller layer | GET /api/tickets, /api/admin/* (if implemented) | TC-014 |
| RQ-06 | Persistent storage is necessary for ticket records | FR-008, NFR-006, BR-009 | SQLiteRepository, ConfigService | Database configuration, persistence layer | TC-015, TC-016 |
| RQ-07 | Demo/sample data is required for local demonstration | FR-009, BR-010, NFR-007 | DemoDataSeeder, StartupInitializer | Application bootstrap / initialization | TC-017, TC-018 |
| RQ-08 | Validation and error handling must be structured and stable | FR-010, FR-011, NFR-003, NFR-007 | ValidationLayer, ErrorHandler | API response validation/error contracts | TC-019, TC-020 |

## 3. Requirement-to-Component Mapping

| Requirement ID | Requirement Summary | Component |
| --- | --- | --- |
| FR-001 | Ticket creation | TicketService, TicketValidator |
| FR-002 | Ticket viewing | TicketQueryService, TicketController |
| FR-003 | Ticket assignment | AssignmentService |
| FR-004 | Work start | WorkProgressService |
| FR-005 | Resolution | ResolutionService |
| FR-006 | Status transition validation | TicketStateMachine |
| FR-007 | Priority handling | TicketValidator, TicketService |
| FR-008 | SQLite persistence and configurable path | SQLiteRepository, ConfigService |
| FR-009 | Demo data seeding and idempotency | DemoDataSeeder, StartupInitializer |
| FR-010 | Structured validation and business errors | ValidationLayer, ErrorHandler |
| FR-011 | Role enforcement | AuthorizationService |
| FR-012 | API/frontend contract | TicketController, UI layer |
| NFR-001 | Performance | Service layer, database layer |
| NFR-002 | Reliability and atomicity | Transaction or state update logic |
| NFR-003 | Security/privacy | AuthorizationService, logging layer |
| NFR-004 | Maintainability | Domain layer/service abstraction |
| NFR-005 | Usability | UI response model, status presentation |
| NFR-006 | Data portability and configuration | ConfigService, SQLite configuration |
| NFR-007 | Testing and quality | Test suite, integration layer |
| NFR-008 | Build/release | Build pipeline, local execution configuration |
| BR-001 to BR-010 | Business rules | Domain validation, state machine |

## 4. API Traceability

The detailed frontend-to-backend contract is defined in docs/api-spec.md. This matrix remains the high-level traceability view of the same requirement-to-endpoint mapping.

| API | Requirement IDs | Purpose |
| --- | --- | --- |
| POST /api/tickets | FR-001, FR-007, FR-010, BR-001, BR-002, BR-007 | Create a ticket with required data and default priority |
| GET /api/tickets/{id} | FR-002, BR-008 | Retrieve ticket state and assignment details |
| POST /api/tickets/{id}/assign | FR-003, FR-006, FR-011, BR-003, BR-004 | Assign one technician to an OPEN ticket |
| POST /api/tickets/{id}/start | FR-004, FR-006, FR-011, BR-005 | Start work on an ASSIGNED ticket |
| POST /api/tickets/{id}/resolve | FR-005, FR-006, FR-011, BR-006 | Resolve an IN_PROGRESS ticket with a non-empty note |
| Startup initialization | FR-009, BR-010 | Seed demo data when DB is empty |
| Database config | FR-008, NFR-006, BR-009 | Configure SQLite file path and ensure runtime DB is excluded from Git |

## 5. Test Cases

| Test Case ID | Scenario | Requirement IDs |
| --- | --- | --- |
| TC-001 | Create valid ticket for active employee and active device | FR-001, BR-001, BR-002 |
| TC-002 | Reject ticket with blank title | FR-001, FR-010, BR-001 |
| TC-003 | Create ticket without priority sets MEDIUM | FR-001, FR-007, BR-007 |
| TC-004 | Assign valid technician to OPEN ticket | FR-003, BR-003, BR-004 |
| TC-005 | Reject duplicate assignment on already assigned ticket | FR-003, FR-006, BR-004 |
| TC-006 | Reject assignment by non-Tech Lead | FR-003, FR-011 |
| TC-007 | Assigned technician starts work successfully | FR-004, BR-005 |
| TC-008 | Non-assigned technician cannot start work | FR-004, FR-011 |
| TC-009 | Resolve IN_PROGRESS ticket with valid note | FR-005, BR-006 |
| TC-010 | Reject resolution with blank note | FR-005, FR-010 |
| TC-011 | Reject OPEN to RESOLVED shortcut | FR-006, BR-008 |
| TC-012 | Reject invalid transition after status resolved | FR-006, BR-008 |
| TC-013 | Accept lifecycle OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED | FR-006, BR-008 |
| TC-014 | Retrieve ticket details by ID | FR-002 |
| TC-015 | Database connection uses configured SQLite path | FR-008, NFR-006 |
| TC-016 | Runtime SQLite database files are excluded from Git | FR-008, NFR-006 |
| TC-017 | Seed demo data on empty database | FR-009, BR-010 |
| TC-018 | No duplicate demo records on restart | FR-009, BR-010 |
| TC-019 | Invalid input returns structured errors | FR-010 |
| TC-020 | Role denial returns stable authorization error | FR-011 |

## 6. Traceability Coverage Summary

- Raw business requirement coverage: 100% for the explicit source requirement set in the repository
- Business rules covered: BR-001 through BR-010
- Functional requirements covered: FR-001 through FR-012
- Non-functional requirements covered: NFR-001 through NFR-008
- Out-of-scope items not covered: category management, search/filter, notifications, attachments, SLA automation, dashboards, external integrations, and multi-technician workflows

## 7. Domain and Data Model Traceability

| Requirement area | Domain model element | JPA entity | SQLite table | Validation/test coverage |
| --- | --- | --- | --- | --- |
| Ticket creation and ownership | User, Ticket | UserEntity, TicketEntity | users, tickets | TC-001, TC-002, TC-003 |
| Device association | Device, Ticket | DeviceEntity, TicketEntity | devices, tickets | TC-001, TC-015 |
| Ticket lifecycle | Ticket, TicketStatus | TicketEntity | tickets | TC-004, TC-007, TC-009, TC-011, TC-012, TC-013 |
| Role-based assignment and resolution | User, UserRole | UserEntity | users | TC-004, TC-006, TC-008, TC-020 |
| Default priority and validation | Priority, Ticket | TicketEntity | tickets | TC-003, TC-019 |
| SQLite persistence and config | Ticket, User, Device | JPA entities | users, devices, tickets | TC-015, TC-016 |
| Demo data initialization | User, Device, Ticket | UserEntity, DeviceEntity, TicketEntity | users, devices, tickets | TC-017, TC-018, TC-DD-01 to TC-DD-05 |
| Audit and timestamps | User, Device, Ticket | entity timestamps | users, devices, tickets | integration validation |

## 8. End-to-End Verification Coverage

| Scenario ID | Requirement IDs | Flow | Verification Status | Evidence |
| --- | --- | --- | --- | --- |
| E2E-01 | N/A | Application startup | BLOCKED | Browser startup was not executed in this session; backend startup was verified by logs. |
| E2E-02 | FR-009, BR-010 | Demo data initialization | PASS | DemoDataInitializer seed logic exists and backend tests started with seeded JPA data successfully. |
| E2E-03 | FR-002 | Ticket list render | PASS (contract-consistent) | Frontend loads known ticket IDs via GET /api/tickets/{id}; no list endpoint exists in the documented API. |
| E2E-04 | FR-002 | Ticket detail view | PASS | Frontend maps backend TicketResponse to the detail panel. |
| E2E-05 | FR-001, FR-007, BR-001, BR-002 | Create ticket | PASS (backend verified) | createTicket integration path was exercised by the backend test suite. |
| E2E-06 | FR-003, FR-004, FR-005, FR-006 | Assignment / work start / resolution lifecycle | PASS (backend verified) | Service and repository validation cover legal state transitions. |
| E2E-07 | FR-001, FR-010 | Validation failure | PASS (backend verified) | Blank-input validation is enforced by backend and surfaced by the global exception handler. |
| E2E-08 | FR-001, FR-010 | Invalid request | PASS | Malformed JSON and invalid parameter handling are mapped to VALIDATION_ERROR. |
| E2E-09 | FR-002, FR-010 | Not found | PASS | getTicket_shouldReturnNotFound_whenMissing is covered in the backend test suite. |
| E2E-10 | FR-003, FR-006, FR-010 | Business-rule failure | PASS | Duplicate assignment and invalid transitions are rejected with BUSINESS_RULE_VIOLATION. |
| E2E-11 | FR-001 to FR-006 | Successful operation | PASS (backend verified) | Lifecycle transitions and ticket operations are exercised in integration tests. |
| E2E-12 | FR-008, BR-009 | Persistence after restart | PASS (backend model verified) | SQLite JDBC + Spring Data JPA persist ticket state across application lifecycle. |
| E2E-13 | FR-009, BR-010 | Restart idempotency | PASS | DemoDataInitializer exits when data already exists. |
| E2E-14 | FR-003, BR-004 | Duplicate prevention | PASS | Duplicate assignment is rejected and state remains unchanged. |

## 9. Gaps and Follow-up Items

The traceability matrix shows explicit coverage for the current requirement set. The following items still require human decision before implementation proceeds:

1. Whether the application should support a simple role header or a more formal identity model
2. Whether ticket status should be fully read-only after RESOLVED or whether reopen workflows are needed
3. Whether a category field is required for future releases
4. Whether a lightweight search/filter feature is expected in the MVP or should be deferred
5. Whether admin users are fixed roles or permission-derived from a user profile model
6. Whether the project should persist a separate ticket history table in a later release
7. Whether live browser automation should be run in CI or local QA before final sign-off
