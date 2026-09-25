# Release notes

## Release version

1.0.0-SNAPSHOT

## Release date

2026-09-25

## Final release gate

NOT READY

This is a local MVP release candidate. It is verified for backend behavior and frontend buildability, but it is not approved for final production release because the repository does not provide complete GitHub issue-traceability evidence and no browser E2E verification was executed.

---

## ⚠ BREAKING CHANGES

- No breaking API changes are introduced in this MVP scope.
- The application remains intentionally limited to a local-demo workflow and does not introduce enterprise identity or multi-tenant behavior.

## ✨ Features

- Ticket creation flow for active requester and active device.
- Ticket viewing by ID.
- Ticket assignment by Tech Lead to an active IT technician.
- Ticket start work by assigned technician.
- Ticket resolution with required non-empty resolution note.
- Role-aware validation and backend enforcement.
- SQLite persistence with configurable database path.
- Demo data seeding for empty database with idempotent initialization.
- Lightweight Vite frontend for guided user interaction.

## 🐛 Bug Fixes

- Removed misleading placeholder configuration that contradicted the actual demo-data implementation.
- Kept frontend calls aligned with the documented API contract instead of inventing extra endpoints.
- Preserved consistent validation and error handling behavior in the backend.

## 📚 Documentation

- Updated requirements and contract documentation to reflect the implemented MVP behavior.
- Added and refreshed release and quality review documentation.
- Documented the actual build/test evidence and release caveats.

## 📦 Build System

- Maven backend package build produces the JAR artifact.
- Frontend production build is configured with Vite.
- Build artifacts are kept separate from source control and runtime DB files.

## 🔧 Operations

- SQLite database path is configurable through `IT_SUPPORT_TICKET_DB_PATH`.
- Backend creates the parent directory for the database automatically.
- Demo data initializes safely when the database is empty.
- Runtime database files are excluded from Git by repository settings.

## 🔧 Miscellaneous

- Repository structure remains aligned to the local MVP architecture.
- Scope stays intentionally limited to the supported support-ticket workflow.

## 🔒 Security

- No committed secrets, passwords, or API keys were found.
- No SQLite database files are tracked in Git.
- Backend validation is enforced server-side before state changes.
- Input validation and role checks are handled in the backend.

---

## Scope

This release covers the local MVP for an internal IT support ticket workflow with:

- Java 22 Spring Boot backend
- SQLite persistence
- JPA/Hibernate repository layer
- REST API contract for ticket creation, lookup, assignment, start, and resolve
- Vite frontend for local demo usage
- seeded demo data when the database is empty

## Requirements covered

- FR-001 through FR-010 from [../01-requirements/Software-requirements.md](Software-requirements.md)
- lifecycle rules and domain constraints from [../02-architecture/domain-model.md](domain-model.md)
- API contract from [api-spec.md](api-spec.md)
- architecture boundary from [../02-architecture/architecture.md](architecture.md)
- traceability from [../01-requirements/requirements-traceability.md](requirements-traceability.md)

## Build and test commands

Backend tests:

```bash
cd it-support-ticket-app/backend
mvn clean test
```

Backend package build:

```bash
cd it-support-ticket-app/backend
mvn clean package
```

Frontend build:

```bash
cd it-support-ticket-app/frontend
npm run build
```

## Artifact

Backend artifact produced by Maven:

- Artifact: it-support-ticket-backend-1.0.0-SNAPSHOT.jar
- Location: it-support-ticket-app/backend/target/it-support-ticket-backend-1.0.0-SNAPSHOT.jar

Frontend build output:

- Location: it-support-ticket-app/frontend/dist/

## Installation and run instructions

1. Start the backend:

```bash
cd it-support-ticket-app/backend
mvn spring-boot:run
```

2. Start the frontend:

```bash
cd it-support-ticket-app/frontend
npm run dev
```

3. Open the frontend in the browser and interact with the local demo flows.

## SQLite configuration

The backend uses SQLite via the configuration in [it-support-ticket-app/backend/src/main/resources/application.yml](../it-support-ticket-app/backend/src/main/resources/application.yml):

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${IT_SUPPORT_TICKET_DB_PATH:./data/it-support-ticket.db}
```

Runtime requirements:

- database path is configurable via the environment variable IT_SUPPORT_TICKET_DB_PATH
- runtime SQLite files are excluded by [.gitignore](../.gitignore)
- the backend creates the parent directory automatically
- the database file is not committed to Git

## Demo-data behavior

The app seeds demo users, devices, and tickets when the database is empty and demo data is enabled. The initializer is idempotent and exits early if records already exist.

Relevant implementation:

- [it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/infrastructure/demo/DemoDataInitializer.java](../it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/infrastructure/demo/DemoDataInitializer.java)
- [it-support-ticket-app/backend/src/main/resources/application.yml](../it-support-ticket-app/backend/src/main/resources/application.yml)

## Known limitations

- Browser-level E2E execution was not performed in this session; no automated browser suite is present in the repository.
- GitHub issue-first workflow could not be fully verified from the repository history; issue templates exist, but actual issue-to-implementation traceability is not demonstrable from the available repo state.
- This is a local MVP and is not a production-ready enterprise support platform.
- The frontend dist output is currently untracked in the working tree and should be excluded before release tagging if this is meant to be a clean source release.

## Verification result

Backend verification:

- Command: `cd "d:\posco-dx\github.com\ntxuan40\it-support-ticket-ai-native\it-support-ticket-app\backend" && mvn clean test`
- Result: BUILD SUCCESS
- Tests run: 20
- Failures: 0
- Errors: 0
- Skipped: 0

Frontend verification:

- Command: `cd "d:\posco-dx\github.com\ntxuan40\it-support-ticket-ai-native\it-support-ticket-app\frontend" && npm run build`
- Result: Vite build succeeded

## Final release note

The project has solid local MVP evidence for backend behavior and build correctness, but it is not approved for final release because E2E browser validation and issue-traceability verification remain incomplete.
