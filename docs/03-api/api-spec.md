# API Specification

- Document status: Draft for review
- Version: 1.0
- Date: 2026-09-24
- Scope: MVP REST API contract for the IT support ticket application

## 1. Purpose

This document defines the backend contract between the frontend and the backend for the ticket workflow. It covers only the required MVP APIs and intentionally excludes out-of-scope features such as search, comments, attachments, notifications, and category management.

## 2. API Design Principles

- Keep the contract simple and explicit.
- Backend owns all business rules and validation.
- Frontend consumes stable JSON payloads and error shapes.
- All state-changing operations are server-authoritative.
- Authentication is intentionally lightweight for MVP and uses actor identity headers.
- No external database or cloud dependency is required.

## 3. Common API Conventions

### 3.1 Base URL

- /api

### 3.2 JSON format

- JSON content type: application/json
- object keys use camelCase
- enum values are uppercase strings
- timestamps use ISO-8601 UTC format: YYYY-MM-DDTHH:mm:ssZ
- null is used only for optional fields

### 3.3 Naming convention

- fields use lowerCamelCase
- endpoint names use lowercase path segments
- enum values use uppercase names: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED

### 3.4 Role and identity model for MVP

Formal identity federation is out of scope. For the MVP, the backend expects request headers containing the acting user context:

- X-User-Id: numeric or string identifier of the caller
- X-User-Role: EMPLOYEE | TECH_LEAD | IT_TECHNICIAN | ADMIN

If identity headers are missing or invalid, the API returns an authorization error.

### 3.5 Error contract

#### Validation error structure

```json
{
  "timestamp": "2026-09-24T10:15:30Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Ticket validation failed",
  "path": "/api/tickets",
  "details": [
    {
      "field": "title",
      "code": "FIELD_REQUIRED",
      "message": "Title is required"
    }
  ]
}
```

#### General error structure

```json
{
  "timestamp": "2026-09-24T10:15:30Z",
  "status": 404,
  "error": "NOT_FOUND",
  "message": "Ticket not found",
  "path": "/api/tickets/9999"
}
```

### 3.6 Error behavior rules

- Validation errors use HTTP 400
- Authorization errors use HTTP 401 or 403
- Not found uses HTTP 404
- Business-rule failures such as invalid transitions or duplicate assignment use HTTP 409
- Unexpected server errors use HTTP 500

### 3.7 Not-found behavior

When a ticket ID does not exist, the API returns a structured not-found response with error code NOT_FOUND and a consistent message.

### 3.8 Business-rule failure behavior

When a business rule is violated, the API returns a stable error code and the ticket remains unchanged.

Examples:

- INVALID_STATUS_TRANSITION
- DUPLICATE_ASSIGNMENT
- ASSIGNMENT_FORBIDDEN
- RESOLUTION_NOTE_REQUIRED
- NOT_ASSIGNED_TECHNICIAN

## 4. Ticket Resource Model

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "MEDIUM",
  "status": "OPEN",
  "assignedTechnicianUserId": null,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:00:00Z"
}
```

## 5. API Endpoints

### API-01: Create ticket

- Requirement ID: FR-001, FR-007, FR-010
- HTTP method: POST
- URL: /api/tickets
- Purpose: Create a new support ticket for an active employee and active device.
- Authentication requirement: Required. Caller identity and role must be present in request headers.
- Path parameters: None
- Query parameters: None
- Request body:

```json
{
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH"
}
```

- Validation:
  - requesterUserId is required
  - deviceId is required
  - title is required and not blank after trimming
  - description is required and not blank after trimming
  - requester must be active
  - device must be active
  - priority, if supplied, must be LOW, MEDIUM, HIGH, or URGENT
  - if priority missing or blank, default to MEDIUM
- Success response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignedTechnicianUserId": null,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:00:00Z"
}
```

- HTTP status: 201 Created
- Error response: validation error or business validation error
- Error code examples:
  - FIELD_REQUIRED
  - DEVICE_INACTIVE
  - USER_INACTIVE
  - INVALID_PRIORITY
  - USER_NOT_ALLOWED
- Example request:

```http
POST /api/tickets
Content-Type: application/json
X-User-Id: 1
X-User-Role: EMPLOYEE

{
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH"
}
```

- Example response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignedTechnicianUserId": null,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:00:00Z"
}
```

### API-02: View ticket by ID

- Requirement ID: FR-002
- HTTP method: GET
- URL: /api/tickets/{id}
- Purpose: Retrieve the current state of a ticket, including assignment and resolution information.
- Authentication requirement: Required. Caller identity and role must be present in request headers.
- Path parameters:
  - id: numeric ticket ID
- Query parameters: None
- Request body: None
- Validation:
  - id must be a valid positive integer
  - ticket must exist
  - actor must be allowed to view the ticket under current role policy
- Success response: full ticket resource
- HTTP status: 200 OK
- Error response: 404 for missing ticket; 403 for unauthorized access
- Error code examples:
  - NOT_FOUND
  - FORBIDDEN
- Example request:

```http
GET /api/tickets/101
X-User-Id: 1
X-User-Role: EMPLOYEE
```

- Example response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "ASSIGNED",
  "assignedTechnicianUserId": 22,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:02:10Z"
}
```

### API-03: Assign technician to ticket

- Requirement ID: FR-003, FR-006, FR-010
- HTTP method: POST
- URL: /api/tickets/{id}/assign
- Purpose: Assign a single active IT technician to an OPEN ticket.
- Authentication requirement: Required. Caller must have TECH_LEAD role in request headers.
- Path parameters:
  - id: ticket ID
- Query parameters: None
- Request body:

```json
{
  "technicianUserId": 22
}
```

- Validation:
  - id must exist
  - ticket must be OPEN
  - caller role must be TECH_LEAD
  - technician must exist and be ACTIVE
  - technician must have role IT_TECHNICIAN
  - ticket must not already have an active assignment
- Success response: updated ticket resource with status ASSIGNED and assignedTechnicianUserId set
- HTTP status: 200 OK
- Error response: 400, 403, 404, 409 depending on case
- Error code examples:
  - INVALID_STATUS_TRANSITION
  - DUPLICATE_ASSIGNMENT
  - ASSIGNMENT_FORBIDDEN
  - USER_INACTIVE
  - TECHNICIAN_NOT_FOUND
- Example request:

```http
POST /api/tickets/101/assign
Content-Type: application/json
X-User-Id: 9
X-User-Role: TECH_LEAD

{
  "technicianUserId": 22
}
```

- Example response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "ASSIGNED",
  "assignedTechnicianUserId": 22,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:02:10Z"
}
```

### API-04: Start work on assigned ticket

- Requirement ID: FR-004, FR-006, FR-010
- HTTP method: POST
- URL: /api/tickets/{id}/start
- Purpose: Move an assigned ticket to IN_PROGRESS when the assigned technician starts work.
- Authentication requirement: Required. Caller must be the assigned technician in the current request context.
- Path parameters:
  - id: ticket ID
- Query parameters: None
- Request body:

```json
{}
```

- Validation:
  - ticket exists
  - ticket status is ASSIGNED
  - caller user id equals assignedTechnicianUserId
- Success response: updated ticket resource with status IN_PROGRESS
- HTTP status: 200 OK
- Error response: 403, 404, 409
- Error code examples:
  - NOT_ASSIGNED_TECHNICIAN
  - INVALID_STATUS_TRANSITION
  - FORBIDDEN
- Example request:

```http
POST /api/tickets/101/start
Content-Type: application/json
X-User-Id: 22
X-User-Role: IT_TECHNICIAN

{}
```

- Example response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignedTechnicianUserId": 22,
  "resolutionNote": null,
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:05:00Z"
}
```

### API-05: Resolve ticket

- Requirement ID: FR-005, FR-006, FR-010
- HTTP method: POST
- URL: /api/tickets/{id}/resolve
- Purpose: Resolve an IN_PROGRESS ticket with a non-empty resolution note.
- Authentication requirement: Required. Caller must be the assigned technician.
- Path parameters:
  - id: ticket ID
- Query parameters: None
- Request body:

```json
{
  "resolutionNote": "Replaced toner cartridge and validated printer connectivity."
}
```

- Validation:
  - ticket exists
  - ticket status is IN_PROGRESS
  - caller is assigned technician
  - resolutionNote is present and not blank after trim
- Success response: updated ticket resource with status RESOLVED and resolutionNote persisted
- HTTP status: 200 OK
- Error response: 400, 403, 404, 409
- Error code examples:
  - RESOLUTION_NOTE_REQUIRED
  - INVALID_STATUS_TRANSITION
  - NOT_ASSIGNED_TECHNICIAN
- Example request:

```http
POST /api/tickets/101/resolve
Content-Type: application/json
X-User-Id: 22
X-User-Role: IT_TECHNICIAN

{
  "resolutionNote": "Replaced toner cartridge and validated printer connectivity."
}
```

- Example response:

```json
{
  "id": 101,
  "ticketNumber": "T-101",
  "requesterUserId": 1,
  "deviceId": 5,
  "title": "Printer not responding",
  "description": "The office printer shows offline and does not accept jobs.",
  "priority": "HIGH",
  "status": "RESOLVED",
  "assignedTechnicianUserId": 22,
  "resolutionNote": "Replaced toner cartridge and validated printer connectivity.",
  "createdAt": "2026-09-24T10:00:00Z",
  "updatedAt": "2026-09-24T10:07:15Z"
}
```

## 6. Validation Rules by Endpoint

- Create ticket validation must reject blank title and blank description.
- Create ticket validation must reject inactive requester and inactive device.
- Assign validation must reject non-Tech Lead actors.
- Assign validation must reject duplicate assignment or already assigned ticket.
- Start validation must reject a non-assigned technician and invalid state transitions.
- Resolve validation must reject blank notes and invalid status transitions.
- All state transitions are validated server-side before persistence.

## 7. Common Response Envelope

The application contracts use a consistent response model for both success and error paths.

### Success response model

```json
{
  "data": {
    "id": 101,
    "status": "OPEN"
  },
  "timestamp": "2026-09-24T10:00:00Z"
}
```

This contract is recommended for the API but may also be simplified to return the resource object directly when the project prefers a direct response. The backend must keep the response structure consistent for the selected implementation.

### Error response model

```json
{
  "timestamp": "2026-09-24T10:15:30Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Ticket validation failed",
  "path": "/api/tickets",
  "details": [
    {
      "field": "title",
      "code": "FIELD_REQUIRED",
      "message": "Title is required"
    }
  ]
}
```

## 8. Traceability Matrix

| Requirement | API endpoint | Backend component | Test |
| --- | --- | --- | --- |
| FR-001, FR-007, FR-010 | POST /api/tickets | TicketService, TicketValidator, TicketController | TC-001, TC-002, TC-003, TC-019 |
| FR-002 | GET /api/tickets/{id} | TicketQueryService, TicketController | TC-014 |
| FR-003, FR-006, FR-010 | POST /api/tickets/{id}/assign | AssignmentService, TicketStateMachine | TC-004, TC-005, TC-006 |
| FR-004, FR-006, FR-010 | POST /api/tickets/{id}/start | WorkProgressService, TicketStateMachine | TC-007, TC-008, TC-011 |
| FR-005, FR-006, FR-010 | POST /api/tickets/{id}/resolve | ResolutionService, TicketStateMachine | TC-009, TC-010, TC-012 |
| FR-008, BR-009 | SQLite configuration and persistence layer | SQLiteRepository, ConfigService | TC-015, TC-016 |
| FR-009, BR-010 | Startup seeding | DemoDataSeeder, StartupInitializer | TC-017, TC-018 |
| NFR-002, NFR-003 | all ticket endpoints | AuthorizationService, ErrorHandler, TransactionManager | TC-020 |

## 9. Implementation Notes

- The API contract remains intentionally minimal.
- No search, comment, attachment, or category endpoints are defined because they are out of scope.
- Response payloads are plain JSON and do not include database-specific fields.
- The backend must not expose internal persistence or ORM objects directly through the API.

## 10. Summary

This API contract supports the required MVP ticket lifecycle:

- create ticket
- view ticket
- assign technician
- start work
- resolve ticket

The contract is intentionally limited to the approved business requirement and is designed to be simple, maintainable, and fully testable with JUnit 5 and SQLite-backed integration tests.
