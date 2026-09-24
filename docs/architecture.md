# Architecture Specification

- Document status: Draft for review
- Version: 1.0
- Date: 2026-09-24
- Repository: it-support-ticket-ai_native
- Application module: it-support-ticket-app

## 1. Purpose

This document defines the software architecture for the AI-native IT support ticket application described by the raw requirements and the SRS. The architecture is intentionally minimal, maintainable, and suitable for a single-team, local-development MVP. It avoids unnecessary complexity such as microservices, external database infrastructure, message brokers, cloud dependencies, and enterprise service integration.

The architecture is constrained by the following decisions:

- Repository contains a single application module under it-support-ticket-app
- Backend uses Java 22, Maven, Spring Boot, REST API, JUnit 5, JPA/Hibernate, and SQLite
- Frontend shall use the simplest maintainable frontend technology compatible with the project requirements
- SQLite is file-based and requires no external database server
- Database path is configurable
- The runtime SQLite file is never committed to Git
- Demo data is initialized automatically at startup when enabled and the database is empty

## 2. Architectural Principles

1. Keep the solution simple and demonstrable.
2. Separate domain logic from infrastructure and UI concerns.
3. Enforce business rules in the backend, not only in the frontend.
4. Use a single persisted store with SQLite for the MVP.
5. Prefer explicit contracts over implicit behavior.
6. Keep the frontend thin and focused on ticket workflow operations.
7. Make demo data safe, repeatable, and idempotent.
8. Prefer local development readiness over production-scale complexity.

## 3. System Overview

The application is a small internal support workflow for handling IT tickets. It supports the following lifecycle:

OPEN -> ASSIGNED -> IN_PROGRESS -> RESOLVED

The system is organized into two primary execution units:

- Backend: Java 22 + Spring Boot + REST API + JPA/Hibernate + SQLite
- Frontend: lightweight UI layer that calls the backend API and presents the ticket states and workflow actions

The system boundary is intentionally limited to the business requirements in scope for the MVP:

- ticket creation
- ticket viewing
- assignment to one technician
- start work
- resolve ticket
- validation and business errors
- SQLite-based persistence
- demo-data initialization

## 4. High-Level Structure

The recommended code layout is:

it-support-ticket-app/
├── backend/
│   ├── src/main/java/
│   ├── src/test/java/
│   ├── pom.xml
│   └── application.properties or application.yml
├── frontend/
│   ├── src/
│   ├── public/
│   └── package.json or equivalent
└── README.md

This structure keeps the backend and frontend independent while preserving a simple repository-level integration model.

## 5. Frontend Architecture

### 5.1 Frontend intent

The frontend is a thin client for the ticket workflow. It does not own domain rules. It is responsible for:

- showing ticket status and workflow information
- collecting input for creating tickets and resolving tickets
- calling backend API endpoints
- displaying validation and business errors clearly
- reflecting role-based actions in the UI

### 5.2 Frontend technology decision

The project requirements do not currently prescribe a specific frontend framework. Therefore, the preferred architecture is a simple, maintainable frontend with minimal complexity, such as:

- plain HTML/CSS/JavaScript for the lightweight MVP, or
- a tiny framework if product requirements later explicitly choose one

The selected frontend technology must remain simple enough that it can be maintained by a small team and does not introduce enterprise tooling overhead.

### 5.3 Frontend responsibilities

- render ticket forms and list/detail views
- call the REST API using structured request/response models
- show status transitions and assigned technician data
- preserve a simple user experience without duplicating business logic
- display safe, user-friendly validation messages

### 5.4 Frontend boundaries

The frontend shall not be the source of truth for ticket business rules. It may enforce convenience validation, but the backend remains authoritative for all state changes.

## 6. Backend Architecture

### 6.1 Runtime model

The backend is a Spring Boot application exposing REST endpoints for ticket operations. It includes a repository layer for persistence, an application/service layer for use cases, and a domain layer for rules and state transitions.

### 6.2 Backend modules

The backend should be organized by concern, for example:

- domain
  - model
  - enums
  - rules and invariants
- application
  - services
  - use-cases
  - validation and workflow orchestration
- infrastructure
  - persistence repositories
  - database configuration
  - startup seeding
- api
  - controllers
  - request/response DTOs
  - exception handlers
- config
  - security/role stubs
  - database config
  - demo-data configuration

## 7. Domain, Application, and Infrastructure Responsibilities

### 7.1 Domain layer

The domain layer owns the fundamental business model and invariants.

Responsibilities:

- Ticket aggregate and ticket status model
- Valid status transitions
- Assignment rules
- Ownership rules
- Resolution rules
- Domain validation logic
- Invariants such as “only the assigned technician may start work” and “RESOLVED is terminal in the MVP”

This layer must not depend on Spring controllers, web DTOs, or database implementation details.

### 7.2 Application layer

The application layer orchestrates use cases and coordinates interactions between domain objects and infrastructure.

Responsibilities:

- create ticket use case
- assign technician use case
- start work use case
- resolve ticket use case
- view ticket use case
- business validation pipeline
- transaction boundaries
- authorization checks using role metadata
- mapping from persistence models to domain objects or DTOs

### 7.3 Infrastructure layer

The infrastructure layer provides technical implementations behind interfaces.

Responsibilities:

- JPA/Hibernate repositories for SQLite persistence
- database configuration
- startup seeding for demo data
- configuration loading
- logging and exception translation

The infrastructure layer is intentionally thin and should not contain business rules.

## 8. Repository Layer

The repository layer is responsible for persistence and storage of ticket data in SQLite.

Responsibilities:

- CRUD operations for tickets
- retrieval by ID
- ticket lookup for active or assigned states
- storing status, priority, technician assignment, resolution note, and timestamps
- enforcing uniqueness constraints for seeded demo data and assignment integrity

Repository contract design:

- define repository interfaces in the application/domain boundary
- implement them using Spring Data JPA or a thin JPA repository abstraction
- keep persistence technology behind interfaces to preserve testability

This prevents domain logic from depending directly on SQLite or JPA-specific behavior.

## 9. Service / Use-Case Layer

The use-case layer is the business orchestration layer between controllers and repositories.

Primary service components:

- TicketService
- AssignmentService
- TicketStateMachine or equivalent workflow validator
- ResolutionService
- DemoDataInitializer
- AuthorizationService

Each use case performs:

- actor validation
- business rule check
- state transition validation
- command execution
- persistence
- structured error response generation

The service layer is the source of truth for state transitions. Controllers only pass requests and return results.

## 10. Controller / API Layer

The API layer exposes REST endpoints for the required workflow.

Recommended endpoints:

- POST /api/tickets
- GET /api/tickets/{id}
- POST /api/tickets/{id}/assign
- POST /api/tickets/{id}/start
- POST /api/tickets/{id}/resolve

Responsibilities:

- accept HTTP requests
- validate input shape
- map to use-case commands
- call application services
- translate domain errors into consistent HTTP responses
- return ticket data or business errors using stable contract shapes

The controller layer must not contain business rules. It should be a thin boundary that delegates to services.

## 11. DTO Strategy

Use dedicated DTOs for request and response payloads.

### Request DTOs

- CreateTicketRequest
- AssignTicketRequest
- StartTicketRequest
- ResolveTicketRequest
- TicketQueryRequest or simple path parameter payloads

### Response DTOs

- TicketResponse
- TicketStatusResponse
- ErrorResponse

DTO guidelines:

- keep API contracts explicit and versionable
- do not expose persistence entities directly to the API
- use trimmed and validated data at the boundary
- keep response shapes simple and consistent

This protects the API contract from persistence-layer changes and reduces accidental leakage of internal fields.

## 12. Error Handling

The system shall use structured error handling across the stack.

### Error model

Each business or validation error must include:

- error code
- human-readable message
- context if needed
- affected entity id when available

Example categories:

- validation errors
- authorization errors
- not found errors
- invalid transition errors
- duplicate assignment errors
- resolution-note validation errors

### Error handling pattern

- Controller catches exceptions or service-level error results
- Global exception handler normalizes them into HTTP responses
- The frontend receives consistent error payloads and does not need special-case parsing logic

### Failure semantics

No state-changing operation may partially mutate ticket data on failure. All status changes must be atomic.

## 13. Validation Strategy

Validation occurs in multiple layers:

- API boundary validation for required fields and payload shape
- service-layer validation for business rules and transitions
- persistence-layer constraint checks for integrity

Examples:

- title and description must not be blank
- requester and equipment must be active
- only the assigned technician can start work
- only the assigned technician can resolve the ticket
- only the valid lifecycle transitions are allowed
- priority defaults to MEDIUM when omitted

The backend is authoritative for validation; the frontend may do convenience checks but cannot trust client-side validation as the security boundary.

## 14. Persistence and SQLite Configuration

### 14.1 Persistence choice

SQLite is the chosen database technology because:

- the project explicitly requires SQLite
- it is file-based and does not require an external DB server
- it matches the lightweight MVP and local demonstration use case
- it reduces operational complexity

### 14.2 Configuration

The database path must be configurable via environment variables or application properties. The configuration must support:

- default local path
- alternate path for local experimentation
- runtime path without source-code changes

Example concept:

- spring.datasource.url=jdbc:sqlite:/path/to/data/it_support_ticket.db

The application configuration must avoid hard-coded database files in the repository.

### 14.3 Git handling

The SQLite file is a runtime artifact and must not be committed to Git. This must be enforced through configuration and repository ignore rules.

### 14.4 Persistence model

Persistent ticket data includes:

- id
- requester id
- equipment id
- title
- description
- priority
- assigned technician id
- current status
- resolution note
- created timestamp
- updated timestamp

## 15. Demo Data Initialization

The application must initialize predefined demo data when the database is empty and when demo mode is enabled.

### Requirements

- auto-seed on startup when the database is empty
- idempotent initialization
- no duplicate demo records after restart
- configurable enable/disable flag
- safe behavior when database already contains real data

### Recommended pattern

- on application startup, detect whether the database has seed data
- if empty and demo mode enabled, insert predefined roles, equipment, users, and sample tickets
- use unique constraints and guarded insert logic to prevent duplicates
- make the seeding step a startup component, not a manual script only

This gives a realistic demo while keeping production behavior predictable.

## 16. Configuration Profiles

At minimum, the project should support profiles such as:

- local
- demo
- test

### local

- SQLite file path for development
- demo data enabled by default
- minimal logging

### demo

- database path configured for sample data
- demo seeding enabled
- deterministic sample set

### test

- isolated SQLite database path
- seeded test fixtures
- no dependence on local developer data

The configuration model must remain simple and avoid unnecessary environment complexity.

## 17. Testing Architecture

Testing is required across multiple layers and must verify real behavior.

### 17.1 Unit tests

- domain state validation
- invalid transition rejection
- priority defaulting
- requirement-specific rule evaluation

### 17.2 Integration tests

- persistence in SQLite
- demo data initialization behavior
- lifecycle flow with real repositories and database state

### 17.3 API tests

- ticket creation request
- ticket fetch
- assignment action
- start-work action
- resolve action
- error response shape

### 17.4 Regression tests

- duplicate demo data is prevented on restart
- invalid transitions do not change stored state
- role restrictions remain enforced

### 17.5 Testing philosophy

Tests shall validate real behavior and not only mock expectations. The system should prefer real SQLite integration for repository tests and minimal mocked boundaries where external dependencies are unnecessary.

## 18. Build Architecture

### 18.1 Backend build

- Java 22
- Maven
- Spring Boot
- JUnit 5

The backend build should produce an executable application artifact that runs in a local environment without a separate database server.

### 18.2 Frontend build

The frontend should be buildable with the project-approved technology without complex tooling. It must remain simple enough that a developer can run it locally and verify API communication quickly.

### 18.3 Dependency management

- backend dependencies are managed by Maven
- frontend dependencies are managed by the frontend toolchain chosen for the project
- no custom monorepo packaging is needed for this MVP

## 19. Deployment and Runtime Model

The runtime model is a local single-host application, not a distributed system.

### Runtime model

- backend application runs as a Spring Boot service
- frontend is served locally or as a lightweight client application
- SQLite database file is stored at a configurable path
- all runtime data stays in the local environment unless intentionally changed by configuration

### Deployment assumptions

- no container orchestration required
- no external database service required
- no cloud runtime required
- no message bus required
- no service discovery required

This is intentionally a small, local deployment model suitable for a one-day or short-cycle MVP.

## 20. Dependency Direction

The dependency direction is intentionally strict:

- frontend -> backend API
- backend API -> application services
- application services -> domain model and repository interfaces
- infrastructure -> repository implementations / database / config / demo-seeding

In plain terms:

- domain does not depend on controllers, UI, or infrastructure
- application depends on domain and abstract repository contracts
- infrastructure depends on application abstractions and framework implementations
- frontend depends only on the backend contract, not on domain internals

This keeps the design testable and maintainable.

## 21. Security Boundaries

This application is not a full enterprise IAM project, but the architecture still needs explicit security boundaries.

Boundaries and controls:

- all role checks are enforced in the backend
- the frontend is treated as untrusted input
- state-changing operations require server-side authorization checks
- sensitive ticket content is not logged unnecessarily
- the system keeps business rules in the server, not in browser logic

The MVP should include role-aware authorization logic even without external user authentication. This protects the business rules from client-side tampering.

## 22. Architecture Decisions

### ADR-01: Single backend service with SQLite

Chosen: one Spring Boot backend with a single SQLite database file.

Reason: project constraints explicitly require SQLite, no external DB server, and local simplicity.

### ADR-02: Thin frontend and backend-owned business logic

Chosen: frontend is for interaction and display; all ticket state rules are enforced in the backend.

Reason: prevents client-side bypass and preserves consistent business behavior.

### ADR-03: layered architecture with domain and application separation

Chosen: separate domain, application, infrastructure, and API layers.

Reason: keeps business behavior explicit and reduces coupling.

### ADR-04: Demo data is startup-driven and idempotent

Chosen: demo records are inserted on startup only when the database is empty and the feature is enabled.

Reason: requirements explicitly require no duplicates after restarts and no permanent seed data in Git.

### ADR-05: No external cloud or distributed infrastructure

Chosen: no microservices, message queue, or external service bus.

Reason: the requirement is an MVP local demo and not a production enterprise service platform.

## 23. Rejected Alternatives

### Rejected: Microservices

Rejected because the scope is a single ticket workflow with no need for independent deployment or scaling boundaries. Microservices would add operational complexity without business value.

### Rejected: External database server

Rejected because the project explicitly requires SQLite and states that no external database server is required.

### Rejected: Full enterprise identity system

Rejected for MVP because the requirement specifies role checks without requiring a full enterprise authentication system.

### Rejected: Complex frontend framework

Rejected unless a project requirement explicitly mandates a framework. A simple frontend is sufficient for local validation and maintainability.

### Rejected: Event-driven or message-queue architecture

Rejected because the workflow is synchronous and local; no asynchronous integration layer is required.

## 24. Risks

- Role enforcement may be under-specified if the project later adds real authentication.
- Without a defined user identity mechanism, UI and API integration may become inconsistent.
- Demo data can drift if the seed specification is not carefully versioned.
- SQLite is easy to operate locally but may not be appropriate for a later production scale-up.
- The raw requirement does not specify category or search/filter needs; future demand could expand the scope.

## 25. Assumptions

- The project is a small MVP and not a large enterprise service platform.
- The frontend will remain simple and not adopt a heavy framework unless a later requirement explicitly changes that.
- The backend is the authoritative source of business logic and validation.
- SQLite is sufficient for local demo and validation purposes.
- Demo data is intended for local demonstration, not for long-lived operational data.
- The repository structure is intentionally minimal and not separated into multiple service repos.

## 26. Non-Goals

- multi-service architecture
- external database infrastructure
- cloud deployment model
- asynchronous distributed processing
- enterprise role management or SSO integration
- SLA automation and escalations
- advanced reporting dashboards
- knowledge base functionality
- email or messaging integrations
- attachments and file management
- category management unless added in a future requirement change

## 27. Summary

This architecture is intentionally lean and business-driven. It keeps the system simple enough to build in a short time, while maintaining clear boundaries between UI, API, application logic, domain rules, and persistence. The result is a maintainable MVP that aligns with the repository requirements, SQLite decision, local demo workflow, and explicit avoidance of unnecessary enterprise complexity.
