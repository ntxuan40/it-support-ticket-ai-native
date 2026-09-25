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
| TicketStatus | OPEN, ASSIGNED, IN_PROGRESS, RESOLVED | TicketStatus enum |
| UserRole | EMPLOYEE, TECH_LEAD, IT_TECHNICIAN, ADMIN | UserRole enum |
| Priority | LOW, MEDIUM, HIGH, URGENT | Priority enum |
| UserStatus | ACTIVE, INACTIVE | UserStatus enum |
| DeviceStatus | ACTIVE, INACTIVE | DeviceStatus enum |

## 5. Test traceability

| Test area | Evidence |
| --- | --- |
| create ticket | TicketControllerIntegrationTest |
| invalid assignment | TicketControllerIntegrationTest |
| invalid transition | TicketControllerIntegrationTest |
| missing ticket | TicketControllerIntegrationTest |
| demo data seeding | DemoDataInitializerTest |

The currently verified backend report states:

- 11 tests run
- 0 failures
- 0 errors
- 0 skipped

## 6. Gaps and constraints

The implementation remains intentionally limited to the repo's current in-scope behavior.

Known boundary conditions:

- there is no documented list endpoint to fetch all tickets
- the frontend loads known ticket IDs and requests detail data individually
- no admin-only endpoint beyond the generic role checks is implemented
- browser-driven E2E verification is separate from backend test verification
