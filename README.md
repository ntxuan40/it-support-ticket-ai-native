# IT Support Ticket AI Native

This repository contains a small internal IT support ticket workflow implemented as a Java 22 Spring Boot backend and a lightweight Vite frontend. The application is designed for local demonstration and validation, not for enterprise-scale production deployment.

## Purpose

The project implements the core support-ticket lifecycle below:

- Employee creates a support ticket for an active device.
- Tech Lead assigns the ticket to one IT technician.
- Assigned technician starts work.
- Assigned technician resolves the ticket with a non-empty resolution note.
- System enforces state transitions and validation rules in the backend.

This is a compact MVP for local development and demo use, with SQLite-backed persistence and seeded data when the database is empty.

## Scope

In scope for the current implementation:

- ticket creation and viewing by ID
- assignment to a single technician
- start-work and resolve actions
- status lifecycle enforcement
- role-based authorization checks
- SQLite persistence
- demo data initialization
- frontend display of workflow states and actions

Out of scope for the current implementation:

- search and filtering beyond basic lookup by ID
- comments, attachments, notification delivery, or SLA automation
- multi-technician assignment
- external identity provider integration
- category management or complex admin dashboards

## Architecture

The repository is divided into:

- root project with Maven parent configuration
- backend module under it-support-ticket-app/backend
- frontend app under it-support-ticket-app/frontend
- documentation under docs

The backend is the source of truth for all business rules. The frontend is intentionally thin and calls the documented REST endpoints. All state changes are validated on the server before persisting to SQLite.

## Technology stack

Backend:

- Java 22
- Maven
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Hibernate + SQLite dialect
- SQLite JDBC
- JUnit 5 / Spring Boot test

Frontend:

- Vite
- Plain HTML/CSS/JavaScript
- Fetch API for backend calls

## Repository structure

```text
.
├── README.md
├── CONTRIBUTING.md
├── pom.xml
├── package-lock.json
├── .gitignore
├── .github/
├── docs/
├── scripts/
├── common_csv_1/
├── common_csv_2/
├── it-support-ticket-app/
│   ├── backend/
│   └── frontend/
└── .vscode/
```

Important notes:

- The root Maven configuration includes the backend module only.
- The frontend is a separate Vite project and is built independently.
- The CSV example directories are separate sample modules and are not part of the main support-ticket application workflow.

## Prerequisites

Required:

- Java 22
- Maven 3.9+
- Node.js + npm for the frontend

Recommended:

- VS Code with Java and JavaScript support
- a local terminal with access to the repository

## Backend setup

From the backend module directory:

```bash
cd it-support-ticket-app/backend
mvn clean test
mvn spring-boot:run
```

The application uses Spring Boot and begins with the default configuration in application.yml.

## Frontend setup

From the frontend directory:

```bash
cd it-support-ticket-app/frontend
npm install
npm run dev
```

For a production-style build:

```bash
cd it-support-ticket-app/frontend
npm run build
```

The frontend is served by Vite and calls the backend at:

```text
http://localhost:8080/api
```

## SQLite setup

The backend uses SQLite through Spring Data JPA. The default configuration is defined in:

```text
it-support-ticket-app/backend/src/main/resources/application.yml
```

Current configuration:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${IT_SUPPORT_TICKET_DB_PATH:./data/it-support-ticket.db}
```

The database location is configurable through the environment variable `IT_SUPPORT_TICKET_DB_PATH`.

If the variable is not set, the default database path is:

```text
./data/it-support-ticket.db
```

relative to the backend working directory. This file is runtime data and is not intended to be committed to Git.

## Database location and configuration

The application is configured with:

- datasource URL: SQLite JDBC URL
- JPA Hibernate dialect: SQLiteDialect
- Hibernate DDL strategy: update
- demo data toggle: enabled by default

The relevant settings are in:

- it-support-ticket-app/backend/src/main/resources/application.yml
- it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/config/DemoDataProperties.java

## Demo data

The backend initializes demo data automatically when the database is empty and the demo-data flag is enabled.

The initializer logic is in:

```text
it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/infrastructure/demo/DemoDataInitializer.java
```

Behavior:

- if demo-data is disabled, no seeding occurs
- if users, devices, or tickets already exist, no duplicate data is inserted
- on empty DB, it seeds sample users, devices, and tickets

This is implemented as an ApplicationRunner and is intentionally idempotent.

## How to run

1. Start the backend:

```bash
cd it-support-ticket-app/backend
mvn spring-boot:run
```

2. Start the frontend in another terminal:

```bash
cd it-support-ticket-app/frontend
npm install
npm run dev
```

3. Open the app in the browser with the Vite dev server.

4. The frontend uses the backend API using the identity headers `X-User-Id` and `X-User-Role`.

## How to test

Backend tests:

```bash
cd it-support-ticket-app/backend
mvn test
```

Frontend build verification:

```bash
cd it-support-ticket-app/frontend
npm run build
```

The project currently has verified backend integration coverage for ticket creation, lifecycle transitions, validation, duplicate assignment, and not-found scenarios.

## How to build

Root build:

```bash
mvn clean package
```

This builds the backend module because the root pom declares the backend as the only Maven module. The frontend is built separately with Vite.

## API overview

The backend exposes the following API surface under `/api`:

- POST /api/tickets
- GET /api/tickets/{id}
- POST /api/tickets/{id}/assign
- POST /api/tickets/{id}/start
- POST /api/tickets/{id}/resolve

Headers required for role-based actions:

- X-User-Id
- X-User-Role

Supported roles:

- EMPLOYEE
- TECH_LEAD
- IT_TECHNICIAN
- ADMIN

Supported ticket states:

- OPEN
- ASSIGNED
- IN_PROGRESS
- RESOLVED

Supported priorities:

- LOW
- MEDIUM
- HIGH
- URGENT

The authoritative API contract is documented in:

- docs/api-spec.md

## Troubleshooting

If the backend fails to start:

- confirm Java 22 is installed and active in PATH
- confirm Maven is installed
- confirm the SQLite file path is writeable
- check that the backend directory has permission to create the data folder

If the frontend cannot reach the backend:

- make sure the backend is running on port 8080
- verify the frontend is calling `http://localhost:8080/api`
- confirm the backend has been started before opening the app

If the database appears to be stale or duplicated:

- remove the runtime SQLite file and restart the backend
- confirm the DB path is not pointing to a tracked project file

## Release information

Current status:

- backend implementation and validation logic are in place
- backend test suite is passing in the project report
- frontend UI is implemented as a thin client around the documented API
- browser-level E2E verification remains a separate execution step when needed

Verified backend result in the current workspace:

- 11 tests run
- 0 failures
- 0 errors
- 0 skipped

Project documentation is intentionally aligned to the actual implementation rather than aspirational features.
