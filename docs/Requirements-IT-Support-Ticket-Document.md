# Requirements document: IT support ticket system

## 1. Business context

The project implements a lightweight internal IT support ticket process for local use. The system allows internal employees to create support requests, managers to assign those requests to a specific technician, and technicians to update the work status until the request is resolved.

## 2. Business requirements

### Requirement 1: ticket creation

An employee may create a support request for a device issue. The request must include the affected device, a short title, and a detailed description. The created ticket is initially in the OPEN state.

### Requirement 2: assignment

A Tech Lead may assign a ticket to a single IT technician. Only one technician may be assigned at a time.

### Requirement 3: work progression

The assigned technician may begin work and move the ticket from ASSIGNED to IN_PROGRESS. The ticket can later be resolved only when it is IN_PROGRESS.

### Requirement 4: lifecycle status

The supported lifecycle is:

OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED

Invalid transitions are rejected.

### Requirement 5: role-based action control

The backend validates the acting user's role and identity. The frontend sends `X-User-Id` and `X-User-Role` headers to authorize each backend action.

### Requirement 6: validation and business errors

The system rejects invalid input, blank notes, duplicate assignment, and other business-rule violations using structured HTTP error responses.

### Requirement 7: persistence and configuration

The system stores data in SQLite and allows configuration through the `IT_SUPPORT_TICKET_DB_PATH` environment variable.

### Requirement 8: demo data

When the database is empty and demo data is enabled, the system seeds sample users, devices, and tickets automatically.

## 3. In-scope behaviors

- create ticket by employee
- view ticket by ID
- assign ticket to an IT technician
- start work by assigned technician
- resolve ticket with a required resolution note
- validate status transitions
- use SQLite persistence
- initialize demo data on an empty database

## 4. Out-of-scope behaviors

The current implementation intentionally does not include:

- search or filtering
- comments or attachments
- SLA automation
- external notifications
- ticket categories
- separate history/event tables
- enterprise identity integration

## 5. Implementation status

The project currently implements the in-scope behaviors listed above and validates them through backend integration tests. The API contract and data model match the implemented code in the backend module and the frontend API usage.
