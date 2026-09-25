# OWASP-Oriented Security Review

## Scope

This review covers the requirements, API specification, architecture, backend production source, backend tests, Maven configuration, GitHub Actions workflow, and application configuration for the IT Support Ticket MVP.

The review is evidence-based. It does not claim that the system is secure merely because an issue was not observed, and it does not introduce assumptions about authentication beyond the documented lightweight identity-header model.

## Findings

### SEC-001 — Ticket view endpoint does not enforce the documented caller context

- **Finding ID:** SEC-001
- **OWASP category:** Broken Access Control (A01)
- **Component/file:** `TicketController.java`, `TicketService.java`; `docs/api-spec.md` API-02
- **Evidence:** API-02 documents authentication as required for `GET /api/tickets/{id}` and requires the actor to be allowed to view the ticket. `TicketController.getTicket` accepts only the path ID and calls `ticketService.getTicketById(id)` without identity headers. `TicketService.getTicketById` loads and returns the ticket without an actor or role check. The existing GET integration test supplies headers, but the controller does not consume them.
- **Security impact:** A caller who knows a ticket ID may retrieve ticket details without the documented authorization context. Ticket descriptions, requester, assignment, and resolution information may be disclosed.
- **Reproduction/verification approach:** Send `GET /api/tickets/{id}` without `X-User-Id` and `X-User-Role` for an existing ticket and observe whether a `200` response is returned.
- **Recommended remediation:** Define and enforce the view authorization policy at the API/service boundary, including required identity headers and role/ownership checks, then add unauthorized-view tests.
- **Severity:** High
- **Status:** Verified

### SEC-002 — Identity headers are caller-supplied and are not authentication

- **Finding ID:** SEC-002
- **OWASP category:** Identification and Authentication Failures (A07)
- **Component/file:** `TicketController.java`, `TicketService.java`; `docs/api-spec.md` section 3.4
- **Evidence:** The API uses `X-User-Id` and `X-User-Role` as the acting-user context. The controller parses the role string, and the service compares the supplied ID with the assigned technician ID, but there is no authentication mechanism proving that the caller controls the supplied identity. The API specification explicitly describes formal identity federation as out of scope and calls this an intentionally lightweight MVP model.
- **Security impact:** If the API is reachable by an untrusted caller, a caller may be able to impersonate another user by supplying that user's ID and role headers. Authorization checks based only on these headers cannot establish caller authenticity.
- **Reproduction/verification approach:** Send a state-changing request with a known or guessed technician ID and the corresponding role header, without presenting any independently authenticated credential. Verify whether the request reaches service authorization as that identity.
- **Recommended remediation:** Treat the header model as a local/demo-only accepted risk, or replace it at the deployment boundary with authenticated identity derived from a trusted session/token. Do not use client-provided identity headers as proof of authentication in a production deployment.
- **Severity:** Medium
- **Status:** Verified

### SEC-003 — Runtime schema mutation is enabled through Hibernate configuration

- **Finding ID:** SEC-003
- **OWASP category:** Security Misconfiguration (A05)
- **Component/file:** `it-support-ticket-app/backend/src/main/resources/application.yml`
- **Evidence:** Runtime configuration sets `spring.jpa.hibernate.ddl-auto: update`. This permits Hibernate to modify the SQLite schema at application startup. The architecture describes a local MVP, but no environment-specific production schema policy is present in the inspected configuration.
- **Security impact:** An unintended or compromised deployment may change the runtime schema automatically, complicating change control, rollback, and database integrity review.
- **Reproduction/verification approach:** Start the application against a copy of the runtime database with an entity/schema difference and observe Hibernate startup behavior under `ddl-auto: update`.
- **Recommended remediation:** Use an explicit migration process or a non-mutating production setting such as `validate` for controlled environments. Keep schema creation/update behavior limited to isolated development/test configuration.
- **Severity:** Medium
- **Status:** Verified

### SEC-004 — Demo data is enabled by default in runtime configuration

- **Finding ID:** SEC-004
- **OWASP category:** Security Misconfiguration (A05) / Sensitive Information Disclosure (A02)
- **Component/file:** `it-support-ticket-app/backend/src/main/resources/application.yml`, `DemoDataInitializer.java`
- **Evidence:** Runtime configuration sets `it.support.ticket.demo-data.enabled: true`. `DemoDataInitializer` inserts fixed sample users, devices, and tickets when the database is empty. The seeded records include user names, email addresses, device locations, ticket descriptions, and resolution information.
- **Security impact:** If the local/demo configuration is deployed beyond its intended trusted environment, predictable sample records may disclose internal-looking data and create known accounts or operational content.
- **Reproduction/verification approach:** Start the application with an empty configured SQLite database and observe that demo records are inserted automatically.
- **Recommended remediation:** Disable demo seeding by default outside explicitly marked local/demo profiles, and require an explicit opt-in for environments that may contain sensitive data.
- **Severity:** Medium
- **Status:** Verified

### SEC-005 — SQL injection evidence was not found in the reviewed persistence path

- **Finding ID:** SEC-005
- **OWASP category:** Injection (A03)
- **Component/file:** `TicketRepository.java`, `UserRepository.java`, `DeviceRepository.java`, `TicketService.java`
- **Evidence:** The reviewed repositories extend Spring Data `JpaRepository`; the service uses repository methods such as `findById`, `count`, `save`, and `saveAll`. No raw SQL string construction or concatenated database query was found in the reviewed backend source.
- **Security impact:** No injection finding is established from the inspected code. This is not a claim that all possible runtime inputs or dependencies are secure.
- **Reproduction/verification approach:** Review repository access paths and run a dedicated security/static analysis scan if injection assurance is required for release evidence.
- **Recommended remediation:** Continue using parameterized repository/query APIs and add a security scan to the release process.
- **Severity:** Not Applicable
- **Status:** No evidence found

### SEC-006 — Input validation is implemented for core workflow fields, but abuse-limit evidence is absent

- **Finding ID:** SEC-006
- **OWASP category:** Input Validation / Injection (A03)
- **Component/file:** `TicketService.java`, request DTOs, `GlobalExceptionHandler.java`
- **Evidence:** `TicketService` checks null and blank requester/device IDs, title, description, and resolution notes; it trims stored title, description, and resolution note. Jackson enum parsing rejects unsupported priority values and the exception handler converts malformed JSON to a structured 400 response. No request-size, field-length, rate-limit, or payload-resource-limit controls were found in the reviewed configuration or tests.
- **Security impact:** Very large or abusive request payloads may consume unnecessary application resources. The review does not establish an exploitable denial-of-service defect, because no load or resource test was performed.
- **Reproduction/verification approach:** Review effective server request limits and execute bounded negative tests for oversized fields/payloads in a controlled environment.
- **Recommended remediation:** Define explicit size/resource limits at the web-server and application boundary if the service will accept untrusted network traffic.
- **Severity:** Low
- **Status:** Not tested

### SEC-007 — Error responses avoid stack traces, but the complete error contract is not security-tested

- **Finding ID:** SEC-007
- **OWASP category:** Security Misconfiguration / Unsafe Error Handling (A05)
- **Component/file:** `GlobalExceptionHandler.java`, `ApiErrorResponse.java`
- **Evidence:** Known exceptions are mapped to status, error code, message, and path. Unexpected exceptions return the generic message `An unexpected server error occurred.` and do not include a stack trace in the response. Existing tests verify several structured fields, but not every exception path or production logging configuration.
- **Security impact:** No direct stack-trace disclosure was found in the response handlers. Incomplete verification leaves residual risk that another framework or configuration path could expose diagnostic details.
- **Reproduction/verification approach:** Exercise representative known and unexpected exception paths and inspect both HTTP responses and application logs without exposing sensitive data.
- **Recommended remediation:** Keep generic external errors, verify production logging levels and redaction, and add contract tests for representative error categories.
- **Severity:** Low
- **Status:** Partial

### SEC-008 — Security event logging and monitoring evidence was not found

- **Finding ID:** SEC-008
- **OWASP category:** Security Logging and Monitoring Failures (A09)
- **Component/file:** `TicketController.java`, `GlobalExceptionHandler.java`, configuration files
- **Evidence:** Authorization failures are converted into HTTP responses, but no explicit audit/security logging for denied actions, invalid identity headers, repeated failures, or sensitive state changes was found in the reviewed source/configuration.
- **Security impact:** Suspicious access attempts and authorization failures may be difficult to investigate after an incident.
- **Reproduction/verification approach:** Trigger denied role/identity actions and inspect application logs and CI/runtime observability for a security event record.
- **Recommended remediation:** Define an appropriate audit policy for authorization failures and sensitive ticket state changes, avoiding ticket-content or credential leakage in logs.
- **Severity:** Low
- **Status:** No evidence found

### SEC-009 — Dependency vulnerability status is not evidenced by the build configuration

- **Finding ID:** SEC-009
- **OWASP category:** Vulnerable and Outdated Components (A06)
- **Component/file:** Root `pom.xml`, backend `pom.xml`
- **Evidence:** The POM specifies Java 22, Spring Boot 3.3.4, SQLite JDBC 3.46.1.0, Hibernate community dialects, and Spring Boot test dependencies. No dependency vulnerability scan, SBOM, or version-policy check is configured in the inspected Maven or CI files.
- **Security impact:** Known vulnerabilities in transitive or direct dependencies may not be detected automatically. No specific vulnerable component is claimed by this review.
- **Reproduction/verification approach:** Run an approved dependency audit such as the organization's Maven/OWASP dependency scanner and review the generated report.
- **Recommended remediation:** Add dependency vulnerability/SBOM scanning to CI as a separate security control and maintain supported dependency versions.
- **Severity:** Not Applicable
- **Status:** Not tested

### SEC-010 — GitHub Actions workflow has supply-chain hardening gaps

- **Finding ID:** SEC-010
- **OWASP category:** CI/CD Security / Software and Data Integrity Failures (A08)
- **Component/file:** `.github/workflows/ci.yml`
- **Evidence:** Actions are referenced by mutable major tags: `actions/checkout@v4` and `actions/setup-java@v4`. The workflow does not declare explicit job or workflow permissions. Pull Requests execute repository code through Maven on a hosted runner.
- **Security impact:** Mutable action references increase exposure to upstream tag changes. Broad or implicit token permissions increase blast radius if workflow execution is compromised. The review found no secret use in this workflow.
- **Reproduction/verification approach:** Inspect the resolved action references and effective `GITHUB_TOKEN` permissions for a Pull Request run; verify whether a dependency/action change can alter the executed build code.
- **Recommended remediation:** Pin third-party actions to reviewed commit SHAs, declare least-privilege permissions such as read-only contents where sufficient, and separate untrusted PR execution from privileged workflows.
- **Severity:** Medium
- **Status:** Verified

### SEC-011 — No hardcoded secret evidence was found in reviewed configuration or workflow files

- **Finding ID:** SEC-011
- **OWASP category:** Secrets Exposure (A02)
- **Component/file:** Configuration files, `.github/workflows/ci.yml`, `.gitignore`, reviewed source/docs
- **Evidence:** No password, API key, access token, private key, or client secret value was found by the reviewed repository search. The workflow does not reference repository secrets. `.gitignore` excludes runtime database files and local data folders.
- **Security impact:** No secret exposure finding is established from the inspected files. Git history and external CI settings were not treated as proof of absence.
- **Reproduction/verification approach:** Run repository secret scanning and review GitHub Actions secret configuration without printing secret values.
- **Recommended remediation:** Keep secret scanning enabled and continue excluding runtime/local artifacts from version control.
- **Severity:** Not Applicable
- **Status:** No evidence found

### SEC-012 — SQLite data protection controls are not evidenced

- **Finding ID:** SEC-012
- **OWASP category:** Data Protection / Sensitive Information Disclosure (A02)
- **Component/file:** `application.yml`, `test application.yml`, SQLite persistence configuration
- **Evidence:** The application stores ticket descriptions, user data, device data, assignments, and resolution notes in a file-based SQLite database. The configuration and architecture specify SQLite and Git ignore rules, but no encryption-at-rest, file-permission policy, retention policy, or backup protection was found.
- **Security impact:** Anyone with filesystem access to the runtime database may read or modify ticket and user data. The source requirements describe a local MVP and do not specify encryption or retention controls.
- **Reproduction/verification approach:** Inspect filesystem permissions and backup handling for the configured database path in the intended deployment environment. Do not copy or exfiltrate database contents.
- **Recommended remediation:** Restrict database file permissions, protect backups, and define encryption/retention requirements before deployment beyond a trusted local environment.
- **Severity:** Medium
- **Status:** Not tested

## Security Risk Summary

### Open findings

- **SEC-001 — High:** Ticket retrieval does not enforce the documented authorization context.
- **SEC-003 — Medium:** Runtime `ddl-auto: update` permits automatic schema mutation.
- **SEC-004 — Medium:** Demo data is enabled by default in runtime configuration.
- **SEC-010 — Medium:** GitHub Actions uses mutable action tags and lacks explicit least-privilege permissions.

### Accepted risks

- **SEC-002 — Medium:** Caller identity is intentionally represented by lightweight request headers for the MVP. This is acceptable only within the documented trusted/local boundary, not as production authentication.
- **SEC-012 — Medium:** SQLite plaintext file storage is consistent with the local MVP design, but filesystem and backup protections remain deployment responsibilities.

### Not Applicable

- **SEC-005:** No raw SQL or string-built query evidence was found in the reviewed persistence path.
- **SEC-009:** No specific vulnerable dependency was established; a dependency audit was not present in the inspected configuration.
- **SEC-011:** No hardcoded secret evidence was found in reviewed files.

### Recommended next actions

1. Enforce authenticated, authorized ticket viewing and define the trusted boundary for identity headers.
2. Pin GitHub Actions to reviewed commit SHAs and declare least-privilege workflow permissions.
3. Disable demo seeding and automatic schema mutation outside explicit local/demo configuration.
4. Add controlled security tests for unauthorized views, mismatched identities, and error responses.
5. Add dependency vulnerability scanning and repository secret scanning to CI.
6. Define SQLite filesystem permissions, backup protection, and data-retention requirements before non-local deployment.
