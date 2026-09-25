# Coding rules and standards

## 1. Scope

This project is intentionally small and local-first. Code should stay simple, explicit, and consistent with the implemented backend contract.

## 2. Core rules

- Do not invent API endpoints that are not in the backend implementation.
- Do not add features outside the current MVP boundary unless the requirement is explicitly documented and implemented.
- Keep business rules on the backend; the frontend is only a thin client.
- Prefer direct, readable Java and plain JavaScript over framework-heavy abstractions.
- Use the documented enums and status values exactly as they are implemented.

## 3. Backend rules

- Use Spring Boot conventions already present in the project.
- Validate user identity and role in the controller/service path when required.
- Keep state transitions guarded in the service layer.
- Return domain-level exceptions that are translated by GlobalExceptionHandler.
- Use structured error responses rather than ad hoc strings for runtime failures.

## 4. Frontend rules

- Call only the documented backend endpoints.
- Use the headers `X-User-Id` and `X-User-Role` for role-based calls.
- Respect the exact enum values: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, LOW, MEDIUM, HIGH, URGENT.
- Treat the backend as the source of truth for ticket state.
- Display user-friendly error messages but do not fabricate backend capabilities.

## 5. Documentation rules

- Documentation must match the current implementation exactly.
- Do not describe unimplemented features as if they already exist.
- If code changes, update the relevant docs before claiming completion.
- When a feature is not verified, label it as unverified or blocked.

## 6. Verification expectation

Every meaningful code change should be validated by:

- backend tests when Java code changes
- frontend build when frontend code changes
- requirement or contract docs updates when behavior changes

The current verified evidence in the workspace remains the backend Surefire report, which recorded 11 tests with 0 failures and 0 errors.
