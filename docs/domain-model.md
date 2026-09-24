# Domain Model

- Document status: Draft for review
- Version: 1.0
- Date: 2026-09-24

## 1. Purpose

This document defines the domain model for the IT support ticket application. It translates the business process into stable domain concepts, entity responsibilities, business rules, and lifecycle behavior. The model follows the requirements in the SRS and keeps the design intentionally minimal for an MVP.

## 2. Domain Boundaries

The domain scope is limited to the following aggregate and supporting entities:

- User
- Device
- Ticket
- TicketStatus enum
- UserRole enum
- UserStatus enum
- DeviceStatus enum
- Priority enum

The domain intentionally excludes category management, search/filter features, comments, attachments, and SLA automation because these are out of scope.

## 3. Core Domain Concepts

### 3.1 User

A User represents any internal actor in the system.

Attributes:

- id: unique user identifier
- fullName: human-readable full name
- email: business contact address
- role: role assigned to user
- status: active or inactive
- createdAt: record creation timestamp
- updatedAt: last update timestamp

Valid roles:

- EMPLOYEE
- TECH_LEAD
- IT_TECHNICIAN
- ADMIN

Valid status:

- ACTIVE
- INACTIVE

Business meaning:

- EMPLOYEE creates tickets
- TECH_LEAD assigns tickets
- IT_TECHNICIAN starts and resolves work
- ADMIN inspects system data and supports demo validation

Business rules:

- A User must be ACTIVE to create tickets or be assigned as technician.
- A User cannot be assigned as a technician unless role is IT_TECHNICIAN.
- A User role is immutable in the MVP unless explicitly expanded later.

### 3.2 Device

A Device represents the equipment tied to the support ticket.

Attributes:

- id: unique device identifier
- assetCode: unique asset or inventory code
- name: device name or label
- deviceType: device class (optional but recommended for MVP)
- location: physical or logical location
- ownerUserId: owning employee or user
- status: active or inactive
- createdAt: record creation timestamp
- updatedAt: last update timestamp

Device status:

- ACTIVE
- INACTIVE

Business rules:

- Only an ACTIVE device may be used in a new ticket.
- A Device must have a unique assetCode within the system.
- Device ownership is kept as a reference to the User entity.

### 3.3 Ticket

A Ticket is the core aggregate of the domain.

Attributes:

- id: unique ticket identifier
- ticketNumber: human-readable or generated number, if explicitly used by the application
- requesterUserId: creator of the ticket
- deviceId: equipment associated with the issue
- title: short summary of the issue
- description: detailed problem description
- priority: LOW, MEDIUM, HIGH, URGENT
- status: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED
- assignedTechnicianUserId: assigned technician, nullable before assignment
- resolutionNote: final note after resolution, nullable until resolved
- createdAt
- updatedAt

Lifecycle:

OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED

Business rules:

- The requester must be an active employee.
- The device must be active and exist.
- Title and description must not be blank after trimming whitespace.
- Priority defaults to MEDIUM if omitted.
- Only one technician may be assigned to a ticket at a time.
- Only the assigned technician may start work on the ticket.
- Only the assigned technician may resolve the ticket.
- A resolution note is required before the ticket can move to RESOLVED.
- RESOLVED is terminal in this MVP.
- Invalid transitions are rejected and the ticket retains its prior valid state.

## 4. Enums

### TicketStatus

- OPEN
- ASSIGNED
- IN_PROGRESS
- RESOLVED

Allowed transitions:

- OPEN -> ASSIGNED
- ASSIGNED -> IN_PROGRESS
- IN_PROGRESS -> RESOLVED

All other transitions are invalid.

### UserRole

- EMPLOYEE
- TECH_LEAD
- IT_TECHNICIAN
- ADMIN

### UserStatus

- ACTIVE
- INACTIVE

### DeviceStatus

- ACTIVE
- INACTIVE

### Priority

- LOW
- MEDIUM
- HIGH
- URGENT

## 5. Aggregate and Relationships

### Ticket aggregate

A Ticket is the aggregate root.

Relationships:

- Ticket belongs to one requester User
- Ticket belongs to one Device
- Ticket may have zero or one assigned technician User
- Only one technician assignment is allowed in the MVP
- Ticket does not contain comments or attachments in this version

Cardinality:

- User 1..* Ticket (as requester)
- Device 1..* Ticket
- User 1..* Ticket (as assigned technician, optional)

## 6. Business Rules by Use Case

### Create ticket

Preconditions:

- requester exists and is ACTIVE
- device exists and is ACTIVE
- title and description are non-blank

Rules:

- system sets status to OPEN
- system sets priority to MEDIUM if not provided
- system stores createdAt and updatedAt

### Assign technician

Preconditions:

- actor has TECH_LEAD role
- ticket exists
- ticket status is OPEN
- target technician exists and is ACTIVE
- target user has IT_TECHNICIAN role

Rules:

- assign technician may happen only once in the MVP
- status changes from OPEN to ASSIGNED
- assignedTechnicianUserId is set

### Start work

Preconditions:

- ticket status is ASSIGNED
- caller is the assigned technician

Rules:

- status changes from ASSIGNED to IN_PROGRESS
- updatedAt is updated

### Resolve ticket

Preconditions:

- ticket status is IN_PROGRESS
- caller is the assigned technician
- resolutionNote is not blank

Rules:

- status changes from IN_PROGRESS to RESOLVED
- resolutionNote is persisted
- updatedAt is updated

## 7. Validation Rules

Required fields:

- User.fullName
- User.email
- User.role
- User.status
- Device.assetCode
- Device.name
- Device.status
- Ticket.requesterUserId
- Ticket.deviceId
- Ticket.title
- Ticket.description

Optional fields:

- Ticket.assignedTechnicianUserId
- Ticket.resolutionNote
- Ticket.ticketNumber, if generated by external pattern
- Ticket.priority, defaults to MEDIUM

Validation behavior:

- trim string input before validation
- reject blank values after trimming
- reject unsupported enums
- reject assignment if actor lacks role or state is invalid
- reject resolution with blank note

## 8. State Transition Rules

| From | To | Condition | Actor |
| --- | --- | --- | --- |
| OPEN | ASSIGNED | valid assignment by Tech Lead | TECH_LEAD |
| ASSIGNED | IN_PROGRESS | assigned technician starts work | IT_TECHNICIAN |
| IN_PROGRESS | RESOLVED | assigned technician resolves with note | IT_TECHNICIAN |

All other transitions are invalid.

## 9. Lifecycle Semantics

The ticket lifecycle is interpreted as follows:

- OPEN: the request has been created and is waiting for assignment
- ASSIGNED: ownership is assigned to a single technician
- IN_PROGRESS: the assigned technician has started handling the issue
- RESOLVED: the issue is resolved with a final note

A resolved ticket is considered complete in the MVP and should not be modified by normal update flows.

## 10. Audit Fields

The minimal audit fields required by the MVP are:

- createdAt
- updatedAt

These fields are required for all persisted entities in the main domain model and support traceability and basic operational review.

There is no requirement for detailed ticket history or event-sourcing in the MVP. A history table may be added later, but it is out of scope for the initial architecture.

## 11. Domain Constraints Summary

The domain model maintains the following invariants:

- only one technician can be assigned to a ticket
- a ticket may not skip lifecycle states
- only valid actors may perform state transitions
- the system never modifies a ticket state without validating business conditions
- invalid transitions do not mutate the ticket

## 12. Domain-to-Entity Mapping Summary

| Domain concept | Domain model | JPA entity | SQLite table |
| --- | --- | --- | --- |
| User | User | UserEntity | users |
| Device | Device | DeviceEntity | devices |
| Ticket | Ticket | TicketEntity | tickets |
| Role | enum | enum | varchar column |
| Ticket status | enum | enum | varchar column |
| Priority | enum | enum | varchar column |

## 13. Deterministic Demo Data Consideration

The domain model supports demo data by defining a small set of canonical reference records:

- one or more active employees
- one or more Tech Leads
- one or more IT technicians
- one or more active devices
- a small number of sample tickets in different lifecycle states

The seeded sample data must not override real user or device records and must be idempotent across restarts.

## 14. Important Notes

- No category field is included because the raw requirement does not define category requirements.
- No comments or attachments are modeled because they are out of scope.
- The model is intentionally simple and suitable for a local-system MVP.
