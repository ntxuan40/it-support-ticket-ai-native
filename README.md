# IT Support Ticket AI Native

A lightweight internal IT support ticket application built as a Java 22 / Spring Boot MVP with SQLite persistence and a simple frontend shell.

## Project goals

- support the ticket workflow defined in the project requirements
- keep the backend authoritative for validation and state transitions
- use SQLite for local file-based persistence with configurable runtime path
- keep the frontend independent and lightweight
- maintain a buildable scaffold before backend business implementation begins

## Repository structure

```text
.
├── README.md
├── CONTRIBUTING.md
├── pom.xml
├── .gitignore
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── PULL_REQUEST_TEMPLATE.md
│   └── workflows/
├── docs/
└── it-support-ticket-app/
    ├── backend/
    └── frontend/
```

## Backend stack

- Java 22
- Maven
- Spring Boot
- REST API
- JPA / Hibernate
- SQLite
- JUnit 5

## Frontend shell

The frontend is scaffolded as an independent Vite-based app with a simple UI placeholder and an explicit build step.

## Local verification

```bash
mvn clean test
cd it-support-ticket-app/frontend
npm install
npm run build
```

## Database configuration

The application is configured to use a runtime SQLite database stored under a local data directory that is ignored by Git. No populated database file should be committed.

## Documentation

The implementation follows the requirements captured in the repository docs, including the SRS, architecture, domain model, data model, and API specification documents.
