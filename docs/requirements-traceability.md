# Requirements traceability

## 1. Purpose

This document maps the implemented ticket workflow to the documented requirements and to the actual backend/frontend code that supports it. It is intentionally limited to features that are implemented and verified in the repository.

## 2. Requirement-to-implementation mapping

| Requirement | Implemented in | Verified behavior |
| --- | --- | --- |
| Ticket creation | TicketService.createTicket, TicketController.createTicket | Validators reject blank required fields and validate active requester/device |
| Ticket lookup | TicketService.getTicketById, TicketController.getTicket | GET /api/tickets/{id} returns ticket data |
| Assignment | TicketService.assignTechnician, TicketController.assignTicket | Only TECH_LEAD can assign an active IT technician to an OPEN ticket |
| Work start | TicketService.startWork, TicketController.startWork | Only assigned technician can move ASSIGNED to IN_PROGRESS |
| Resolution | TicketService.resolveTicket, TicketController.resolveTicket | Only assigned technician can resolve IN_PROGRESS with non-empty note |
| Lifecycle validation | TicketStateMachine + service checks | OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED only |
| Priority defaulting | TicketService.createTicket | Missing priority defaults to MEDIUM |
| SQLite persistence | application.yml + JPA repositories | SQLite is configured as the runtime database |
| Demo data | DemoDataInitializer | Seeded only when DB is empty |
| Structured errors | GlobalExceptionHandler | Validation, not-found, role, and business-rule errors are mapped |

## 3. API traceability

| Endpoint | Purpose | Implemented |
| --- | --- | --- |
| POST /api/tickets | create a ticket | Yes |
| GET /api/tickets/{id} | get ticket by ID | Yes |
| POST /api/tickets/{id}/assign | assign a technician | Yes |
| POST /api/tickets/{id}/start | start work | Yes |
| POST /api/tickets/{id}/resolve | resolve a ticket | Yes |

The frontend only calls these endpoints and does not invent other API routes.

## 4. State and enum traceability

| Domain concept | Value set | Implementation |
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

## 8. Gaps and Follow-up Items

The traceability matrix shows explicit coverage for the current requirement set. The following items still require human decision before implementation proceeds:

1. Whether the application should support a simple role header or a more formal identity model
2. Whether ticket status should be fully read-only after RESOLVED or whether reopen workflows are needed
3. Whether a category field is required for future releases
4. Whether a lightweight search/filter feature is expected in the MVP or should be deferred
5. Whether admin users are fixed roles or permission-derived from a user profile model
6. Whether the project should persist a separate ticket history table in a later release
