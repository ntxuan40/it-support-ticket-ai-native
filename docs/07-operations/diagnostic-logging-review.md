# Diagnostic Logging and Failure Diagnosis Review

## Scope and Evidence

This review covers:

- `GlobalExceptionHandler` and `ApiErrorResponse`.
- `TicketController` and `TicketService` request/business flow.
- Existing exception classes and test assertions.
- Maven/Surefire test output and reports.
- `.github/workflows/ci.yml`.
- Runtime/test application configuration.

No logging code was changed. No sensitive data was added to logs or examples.

## Classification

- **Good:** The reviewed evidence provides enough useful information for the stated diagnostic need.
- **Needs Improvement:** Useful evidence exists, but important diagnostic context or operational handling is incomplete.
- **Missing:** The reviewed source/configuration does not provide the required diagnostic evidence.

## 1. Failure Identification

- **Classification:** Needs Improvement
- **Evidence:** `ApiErrorResponse` includes `timestamp`, `status`, `error`, `message`, and `path`. `GlobalExceptionHandler` maps known exception types to stable response codes. Maven/Surefire output identifies the test class, test method, assertion failure, expected value, actual value, and stack-trace location.
- **Assessment:** API failures and automated test failures can usually be identified. Runtime application failures are less diagnosable because no explicit application error logging or correlation identifier was found.
- **Recommendation:** Add structured runtime error logging at the exception boundary with a correlation/request identifier, HTTP method, path, exception category, and safe operation metadata. Do not log request bodies, credentials, or full ticket content by default.

## 2. Operation Identification

- **Classification:** Needs Improvement
- **Evidence:** Error responses include the request path, and controller routes identify create, view, assign, start, and resolve operations. Service exception messages identify some failed business operations, such as `Only OPEN tickets can be assigned.` and `Ticket must be IN_PROGRESS before it can be resolved.`
- **Assessment:** The path often identifies the operation, but runtime logs do not provide a consistent operation/event name. The error response does not include the HTTP method, and service errors do not consistently include an explicit use-case identifier.
- **Recommendation:** Record a safe operation name such as `ticket.assign`, `ticket.start`, or `ticket.resolve` together with HTTP method and path in structured logs. Keep identifiers limited to non-sensitive metadata needed for diagnosis.

## 3. Request and Business Context

- **Classification:** Needs Improvement
- **Evidence:** The error response includes the request URI and timestamp. Controller methods receive actor headers for state-changing operations, and service messages identify validation or business-rule causes. `TicketResponse` contains ticket and assignment state for successful responses.
- **Assessment:** Failure responses do not include a correlation ID, HTTP method, actor role, actor ID, ticket ID as a separate field, current status, or requested transition. Logging of these fields is also not implemented. The missing context makes it harder to distinguish repeated operations or correlate a failure across services/logs.
- **Recommendation:** Add safe structured context at the request/exception boundary: correlation ID, operation, HTTP method, path, actor role, non-secret actor identifier, entity ID, and relevant current/requested state. Apply redaction and retention rules before enabling it.

## 4. Stack Traces

- **Classification:** Needs Improvement
- **Evidence:** Surefire output for a failed assertion includes the failure message and Java stack trace, including the test method and source location. `GlobalExceptionHandler.handleUnexpected` returns a generic HTTP error and does not log the received exception. Known validation and business exceptions are expected control-flow outcomes and are mapped without stack traces.
- **Assessment:** CI test failures have useful stack traces. Runtime unexpected failures may lose their server-side stack trace because no logger call is present at the global exception boundary. Returning no stack trace to the client is appropriate, but suppressing it from controlled server logs reduces RCA capability.
- **Recommendation:** Log unexpected exceptions server-side with the exception object and correlation ID while returning the existing generic response externally. Keep expected validation/authorization/business failures at an appropriate non-error log level or structured audit level without noisy stack traces.

## 5. Sensitive Information Risk

- **Classification:** Good with a remaining review requirement
- **Evidence:** `GlobalExceptionHandler` returns controlled messages for persistence and unexpected failures and does not serialize stack traces. No request body logging or explicit ticket-content logging was found. The reviewed repository search found no hardcoded secrets. The application does print the configured database directory when creating it in `DatabaseProperties`.
- **Assessment:** The reviewed error responses and application code do not show direct ticket-content or credential logging. Absolute database path output is operational metadata and may reveal filesystem layout in logs; its sensitivity depends on the deployment environment. The absence of logging code is not proof that external server/proxy logs are safe.
- **Recommendation:** Define log redaction rules before adding request/business context. Avoid request bodies, resolution notes, descriptions, identity credentials, and database contents in logs. Review filesystem paths and proxy/access logs in the deployment environment.

## 6. CI Failure Diagnosis and RCA

- **Classification:** Needs Improvement
- **Evidence:** `.github/workflows/ci.yml` runs `mvn clean test`. Maven/Surefire output identifies test classes, test methods, counts, failures, errors, and assertion stack traces. `../08-incident-learning/WO-401-evidence.md` records a controlled incorrect assertion, failed local/CI verification, restoration, and passing CI. The PR template requires the verification command and observed result.
- **Assessment:** The current CI output is sufficient to identify ordinary compilation and test assertion failures. The workflow does not explicitly upload Surefire reports, publish a summarized artifact, classify infrastructure versus code/test failures, or attach an RCA record. Maven is invoked directly and the Maven version is not pinned, which can complicate reproducibility.
- **Recommendation:** Preserve Surefire reports or a concise test summary as CI artifacts, record the failing command and failure classification in the PR/work-order evidence, and use an RCA record for recurring or release-blocking failures. Pin Maven when reproducibility across runner images is required.

## 7. Error Handling and Exception Mapping

- **Classification:** Good for external response safety; Needs Improvement for operational diagnosis
- **Evidence:** `GlobalExceptionHandler` has mappings for not found, forbidden, business-rule, validation, malformed JSON, type mismatch, data-integrity, persistence, and unexpected exceptions. `ApiErrorResponse` provides stable status, error code, message, path, timestamp, and details fields.
- **Assessment:** The external error contract is consistent enough to identify common failure categories without exposing stack traces. The `details` list is present but the reviewed handlers do not populate field-level details for these custom exceptions. Operational logs are not produced by the handler.
- **Recommendation:** Keep the external response safe and stable. Add field-level details only where the contract requires them, and add internal structured exception logging for unexpected failures and security-relevant denials.

## 8. Business Event Information

- **Classification:** Missing
- **Evidence:** The service mutates ticket state and sets timestamps, but no explicit business-event logging or audit event record was found for creation, assignment, start, resolution, rejection, or failed transition. The architecture excludes a separate history/event table for the MVP.
- **Assessment:** Current logs cannot provide a reliable event timeline for a ticket or explain who attempted each state change after the request completes.
- **Recommendation:** If operational auditability becomes a requirement, define a minimal event/audit policy first. Record actor context and transition metadata without logging ticket descriptions, resolution content, or credentials. Do not assume that adding verbose logs is equivalent to an audit trail.

## 9. Test Failure Reports

- **Classification:** Good for current Maven test scope
- **Evidence:** Maven Surefire identifies individual test classes and produces per-test reports under `target/surefire-reports`. The controlled WO-401 failure showed the expected/actual JSON values and the failing test method/source location.
- **Assessment:** The current reports are adequate for ordinary test assertion, compilation, and test execution failures. They are generated artifacts and are not configured as durable GitHub Actions artifacts in the workflow.
- **Recommendation:** Retain the current Surefire diagnostics and publish them as CI artifacts if the project needs post-run evidence after log expiration or for richer RCA.

## Summary

| Area | Classification | Key conclusion |
| --- | --- | --- |
| Failure identification | Needs Improvement | API/test failures are identifiable; runtime exception logging is limited. |
| Operation identification | Needs Improvement | Path and messages help, but no consistent operation name or method context exists. |
| Request/business context | Needs Improvement | Path/timestamp exist; correlation and safe actor/business context are missing. |
| Stack traces | Needs Improvement | CI has traces; unexpected runtime exceptions are not explicitly logged. |
| Sensitive information | Good | No direct sensitive-content logging evidence; path/log environment review remains necessary. |
| CI failure diagnosis | Needs Improvement | Maven output is useful, but reports/artifacts and failure classification are not configured. |
| Exception mapping | Good / Needs Improvement | External mapping is structured; operational logging and field details are limited. |
| Business events | Missing | No event/audit timeline is implemented. |
| Test reports | Good | Surefire identifies failing tests and assertion locations. |

## Diagnostic Review Conclusion

The system can diagnose most ordinary automated test failures and can return useful structured API errors. Runtime diagnosis is weaker because the application does not explicitly log unexpected exceptions, correlation context, operation names, or business events. CI diagnosis is adequate for the current Maven test gate but would be stronger with retained Surefire artifacts and explicit failure classification. These recommendations do not require logging sensitive ticket content.
