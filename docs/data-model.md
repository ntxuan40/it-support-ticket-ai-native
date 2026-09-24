# Data Model and SQLite Mapping

- Document status: Draft for review
- Version: 1.0
- Date: 2026-09-24

## 1. Scope

This document defines the physical data model for the MVP. It maps the domain model to JPA entities and then to SQLite tables. The model is intentionally small and aligned with the business requirements and architecture.

## 2. Mapping Strategy

Domain -> JPA Entity -> SQLite Table

Example mapping flow:

- User domain aggregate -> UserEntity -> users
- Device domain concept -> DeviceEntity -> devices
- Ticket domain aggregate -> TicketEntity -> tickets

The mapping is straightforward because the MVP does not require complex inheritance or separate event tables.

## 3. Entity-to-Table Mapping

### 3.1 User

Domain: User
JPA entity: UserEntity
SQLite table: users

Purpose:

- stores actors for the workflow
- supports ticket creation, assignment, and resolution checks

Table definition:

| Column | Type | Nullable | PK | Unique | FK | Index | Default | Business meaning |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| id | BIGINT | No | Yes | No | No | Yes | auto | primary identifier for the user |
| full_name | VARCHAR(255) | No | No | No | No | Yes | none | display name of the user |
| email | VARCHAR(255) | No | No | Yes | No | Yes | none | business email; unique across users |
| role | VARCHAR(50) | No | No | No | No | Yes | none | role: EMPLOYEE, TECH_LEAD, IT_TECHNICIAN, ADMIN |
| status | VARCHAR(20) | No | No | No | No | Yes | ACTIVE | active or inactive state |
| created_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | last update timestamp |

Notes:

- Unique constraint on email prevents duplicate user records.
- Index on role and status helps filtering and authorization checks.
- status defaults to ACTIVE for new users.

### 3.2 Device

Domain: Device
JPA entity: DeviceEntity
SQLite table: devices

Purpose:

- stores equipment data associated with a ticket

Table definition:

| Column | Type | Nullable | PK | Unique | FK | Index | Default | Business meaning |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| id | BIGINT | No | Yes | No | No | Yes | auto | primary identifier for the device |
| asset_code | VARCHAR(100) | No | No | Yes | No | Yes | none | unique asset code for the equipment |
| name | VARCHAR(255) | No | No | No | No | Yes | none | human-readable device name |
| device_type | VARCHAR(100) | Yes | No | No | No | Yes | none | optional device class such as printer, PC, VM |
| location | VARCHAR(255) | Yes | No | No | No | Yes | none | room, site, or department location |
| owner_user_id | BIGINT | Yes | No | No | Yes (users.id) | Yes | none | user responsible for the device |
| status | VARCHAR(20) | No | No | No | No | Yes | ACTIVE | ACTIVE or INACTIVE |
| created_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | last update timestamp |

Notes:

- asset_code is unique and acts as a natural identity for device lookup.
- owner_user_id is optional because a device may be tracked independently of a current owner.
- device_type is optional to keep the MVP lightweight.

### 3.3 Ticket

Domain: Ticket
JPA entity: TicketEntity
SQLite table: tickets

Purpose:

- permanent storage of support requests and their state

Table definition:

| Column | Type | Nullable | PK | Unique | FK | Index | Default | Business meaning |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| id | BIGINT | No | Yes | No | No | Yes | auto | primary identifier for the ticket |
| ticket_number | VARCHAR(50) | Yes | No | Yes | No | Yes | none | optional human-readable ticket number |
| requester_user_id | BIGINT | No | No | No | Yes (users.id) | Yes | none | employee who created the ticket |
| device_id | BIGINT | No | No | No | Yes (devices.id) | Yes | none | device related to the issue |
| title | VARCHAR(255) | No | No | No | No | Yes | none | summary of the issue |
| description | TEXT | No | No | No | No | Yes | none | detailed problem report |
| priority | VARCHAR(20) | No | No | No | No | Yes | MEDIUM | LOW, MEDIUM, HIGH, URGENT |
| status | VARCHAR(20) | No | No | No | No | Yes | OPEN | OPEN, ASSIGNED, IN_PROGRESS, RESOLVED |
| assigned_technician_user_id | BIGINT | Yes | No | No | Yes (users.id) | Yes | none | active technician assigned to the ticket |
| resolution_note | TEXT | Yes | No | No | No | No | none | final resolution summary |
| created_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | creation timestamp |
| updated_at | TIMESTAMP | No | No | No | No | No | CURRENT_TIMESTAMP | last update timestamp |

Notes:

- requester_user_id must reference an active employee.
- device_id must reference an active device.
- assigned_technician_user_id is nullable until assignment occurs.
- status defaults to OPEN.
- priority defaults to MEDIUM.
- ticket_number may be generated externally or by application logic; if not used, it may be omitted in the MVP implementation.

## 4. Database Constraints

### 4.1 Primary keys

- users.id
- devices.id
- tickets.id

### 4.2 Unique constraints

- users.email unique
- devices.asset_code unique
- tickets.ticket_number unique if generated and used

### 4.3 Foreign keys

- tickets.requester_user_id -> users.id
- tickets.device_id -> devices.id
- tickets.assigned_technician_user_id -> users.id
- devices.owner_user_id -> users.id

### 4.4 Indexes

Recommended indexes:

- users.role
- users.status
- devices.status
- devices.owner_user_id
- tickets.status
- tickets.requester_user_id
- tickets.device_id
- tickets.assigned_technician_user_id
- tickets.created_at

## 5. JPA Entity Mapping Notes

### 5.1 UserEntity

Recommended JPA annotations:

- @Entity
- @Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
- @Enumerated(EnumType.STRING)
- @Column(nullable = false)

### 5.2 DeviceEntity

Recommended JPA annotations:

- @Entity
- @Table(name = "devices", uniqueConstraints = @UniqueConstraint(columnNames = "asset_code"))
- @ManyToOne(fetch = FetchType.LAZY)
- @JoinColumn(name = "owner_user_id", referencedColumnName = "id")

### 5.3 TicketEntity

Recommended JPA annotations:

- @Entity
- @Table(name = "tickets")
- @ManyToOne(fetch = FetchType.LAZY)
- @JoinColumn(name = "requester_user_id", nullable = false)
- @ManyToOne(fetch = FetchType.LAZY)
- @JoinColumn(name = "device_id", nullable = false)
- @ManyToOne(fetch = FetchType.LAZY)
- @JoinColumn(name = "assigned_technician_user_id")
- @Enumerated(EnumType.STRING)
- @Column(nullable = false)

## 6. Sample SQLite Table Summary

The physical model is minimal and aligned with the domain model:

- users
- devices
- tickets

This is sufficient for the MVP because the domain does not require comments, attachments, or activity history tables.

## 7. Business Meaning per Table

### users

Stores all internal users and their role-based access. This table is essential for ticket ownership and authorization checks.

### devices

Stores all equipment items that may be tied to a support incident. It is the system of record for ticket-related hardware.

### tickets

Stores the actual support request, assignment, status, and resolution details. It is the central operational record of the workflow.

## 8. Data Integrity Rules

The data model enforces the following integrity rules:

- foreign keys hold user and device references
- email unique for users
- asset code unique for devices
- ticket states enforce valid transitions through application logic
- assigned technician may reference only a valid IT technician user
- a resolved ticket cannot be mutated by ordinary update flows

## 9. Demo Data Strategy

### Requirement summary

The application shall initialize predefined demo users, devices, and tickets automatically when enabled and the database is empty. The strategy must be deterministic, idempotent, and safe.

### Deterministic strategy

Use a startup seeder that inserts a fixed set of records with stable IDs or natural keys based on known emails and asset codes.

Example seed design:

- employees: alice@company.local, bob@company.local
- tech lead: sam@company.local
- technicians: jenny@company.local, marco@company.local
- devices: PRN-1001, PC-2001, VM-3001
- tickets: T-1001, T-1002, T-1003

### Rule set

- if the database is empty and app.demo-data.enabled=true, insert seed records
- if app.demo-data.enabled=false, do not insert seed records
- if the database already contains data, do not overwrite existing users or devices
- if a seed record already exists by natural key, skip insertion
- if startup is repeated, no duplicate records should appear
- never commit a runtime .db file to Git

### Example configuration concept

- app.demo-data.enabled=true
- app.database.path=./data/it-support-ticket.db

The final property name should follow the project’s established convention if a different naming pattern already exists.

## 10. Demo Data Test Cases

### TC-DD-01: empty database initialization

Given a freshly created SQLite database and demo data enabled, when the application starts, then the seed users, devices, and sample tickets are inserted exactly once.

### TC-DD-02: non-empty database

Given a SQLite database that already contains users or tickets, when the application starts with demo data enabled, then the startup seeder must not overwrite or duplicate existing records.

### TC-DD-03: repeated startup

Given the same database and the same seed configuration, when the application is restarted, then the demo data is not duplicated.

### TC-DD-04: duplicate prevention

Given a natural key conflict such as the same email or asset code, when seeding runs, then the duplicate record is ignored instead of inserted.

### TC-DD-05: demo-data disabled

Given app.demo-data.enabled=false, when the application starts, then no demo users, devices, or tickets are inserted.

## 11. Non-Requirements and Future Extensions

The current data model explicitly does not include:

- comments
- attachments
- ticket activity history tables
- category taxonomy
- SLA tables
- dashboards or reporting tables
- user sessions or identity tables

These should be designed separately if the business adds them in a later release.

## 12. Summary

The SQLite data model stays intentionally small and aligned with the business requirements:

- users table for people and roles
- devices table for equipment
- tickets table for the full ticket workflow

The design enforces the lifecycle, validation, and data integrity constraints while keeping the implementation simple and maintainable for the MVP.
