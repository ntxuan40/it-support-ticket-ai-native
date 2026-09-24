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

## 7. Gaps and Follow-up Items

The traceability matrix shows explicit coverage for the current requirement set. The following items still require human decision before implementation proceeds:

1. Whether the application should support a simple role header or a more formal identity model
2. Whether ticket status should be fully read-only after RESOLVED or whether reopen workflows are needed
3. Whether a category field is required for future releases
4. Whether a lightweight search/filter feature is expected in the MVP or should be deferred
5. Whether admin users are fixed roles or permission-derived from a user profile model
