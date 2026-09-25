# Root Cause Analysis — Ticket View Authorization Defect

## 1. Problem Statement

The ticket-view flow does not enforce the documented caller identity and authorization requirement.

`GET /api/tickets/{id}` is documented as requiring identity headers and an actor allowed to view the ticket. The current controller method accepts only the ticket ID and the service retrieves the ticket without actor or role authorization.

This defect was identified during end-to-end verification and security review. No production code was changed during detection or this RCA.

## 2. Expected Behavior

The requirement source states that the backend validates the acting user's role and identity and that identity headers authorize backend actions.

API-02 in `../03-api/api-spec.md` further specifies that ticket viewing requires:

- Caller identity and role headers.
- A valid ticket ID.
- An actor allowed to view the ticket under the current policy.
- A structured authorization error for an unauthorized view.

The expected behavior is that a request without valid caller context or without view permission is rejected and does not disclose the ticket resource.

## 3. Actual Behavior

`TicketController.getTicket` has the following effective contract:

- It receives only `@PathVariable("id") Long id`.
- It does not receive `X-User-Id` or `X-User-Role`.
- It calls `ticketService.getTicketById(id)`.

`TicketService.getTicketById`:

- Finds the ticket by ID.
- Maps and returns the ticket.
- Performs no actor identity or role authorization check.

The existing GET integration test supplies identity headers, but those headers are not consumed by this endpoint. A caller who knows a ticket ID can therefore receive the ticket response without the documented view authorization check.

## 4. Detection

- **Test:** Existing tests passed, but did not test unauthorized or missing-header viewing.
- **CI:** `mvn clean test` passed with 20 tests; CI therefore did not detect the defect.
- **E2E:** End-to-end verification compared the API requirement with the implementation boundary and stopped when the mismatch was found.
- **Manual verification:** Code inspection confirmed that the controller/service path has no actor authorization input or check.
- **Other:** The OWASP review recorded the same issue as SEC-001, Broken Access Control.

## 5. Timeline

1. **Requirement:** The requirement and API contract defined role/identity-controlled backend actions and authorized ticket viewing.
2. **Implementation:** The ticket view controller/service path was implemented as ID-only retrieval without actor authorization.
3. **AI assistance:** AI-assisted analysis and documentation were part of the repository governance context, but the exact contribution to this implementation is not recorded.
4. **Review:** The repository records human review and PR #17 review/merge evidence, but no review record specifically shows that the GET authorization boundary was challenged.
5. **Test:** The existing integration test verified successful retrieval and not-found behavior, but not unauthorized retrieval.
6. **CI:** `mvn clean test` passed because the existing test scenarios passed.
7. **Detection:** E2E verification and the OWASP review identified the requirement-to-implementation mismatch.

## 6. Root Cause

### Symptom

A ticket can be returned by ID without the documented caller authorization context.

### Direct Cause

`TicketController.getTicket` and `TicketService.getTicketById` do not accept or validate actor identity/role for the view operation.

### Root Cause

The authorization requirement for ticket viewing was not represented in the implementation method contract and was not covered by an unauthorized-view automated scenario. The API contract and implementation therefore diverged while the happy-path retrieval test remained green.

### Contributing Factors

- The existing GET test sent headers but did not assert that they affected authorization.
- Test design covered successful retrieval and not-found behavior but left view authorization as a gap.
- CI validated only the existing automated scenarios; it could not detect an untested authorization rule.
- The lightweight identity-header model made it important to test the boundary explicitly, but the missing-header/invalid-header paths were not exercised for GET.
- Human review evidence confirms review/merge occurred, but does not show a specific checklist item confirming authorization for every API endpoint.

## 7. AI Contribution

The repository establishes that AI may assist with analysis, implementation candidates, tests, and documentation, while human approval is required. The evidence does not identify which exact implementation or review text was AI-generated.

- **AI generated:** Unknown / Requires Investigation.
- **AI suggested:** Unknown / Requires Investigation.
- **Human modified:** Unknown / Requires Investigation.
- **Human approved:** PR #17 was recorded as reviewed and merged, but approval of this specific authorization behavior is not separately evidenced.

This RCA does not attribute the defect to AI. The failure is a requirements-to-implementation and coverage gap that required human review to detect.

## 8. Escape Point

The defect was not detected earlier because:

- The successful GET test proved retrieval, not authorization.
- The test supplied headers that the controller ignored, creating misleading context without a negative authorization assertion.
- No test requested an existing ticket without identity headers or with an unauthorized actor.
- The CI gate ran correctly but had no scenario capable of exposing this defect.
- The review evidence did not demonstrate an endpoint-by-endpoint authorization checklist.

## 9. Corrective Action

The direct corrective action is to align the view operation with the documented authorization contract:

1. Define the allowed view policy for the MVP.
2. Pass the actor identity/role into the view use case.
3. Enforce the policy before returning the ticket.
4. Return the documented structured authorization error when access is denied.
5. Preserve the existing not-found behavior without disclosing unauthorized resource details.

Implementation and production-code changes are outside this RCA document and require a separately scoped fix.

## 10. Preventive Action

### Test

- Add an API test for missing identity headers on ticket view.
- Add an API test for an actor who is not allowed to view the ticket.
- Assert HTTP status, error code, message, path, and unchanged/disclosed state as appropriate.
- Keep the successful retrieval test, but do not treat it as authorization coverage.

### Review

- Add an endpoint-by-endpoint authorization check to the human review checklist.
- Require reviewers to confirm that headers or authenticated context are actually consumed, not merely present in test requests.

### Specification

- Define the MVP view policy explicitly: which roles can view which tickets and how ownership is determined.
- Keep the API contract and implementation method signature aligned.

### CI

- Keep `mvn clean test` as the execution gate.
- Require the new unauthorized-view tests to run in the existing CI job.
- No workflow change is required for this preventive action; the missing control is the test scenario, not failure propagation.

### Prompt

- Require AI-assisted test generation to enumerate positive and negative authorization scenarios for every endpoint marked as authenticated.
- Require AI review outputs to identify endpoints where request headers are present in tests but not consumed by production code.

### Governance

- Add authorization coverage status to requirement-to-test traceability.
- Do not close a Work Order while a documented authorization requirement is marked only by a happy-path test.
- Record residual authorization gaps in Work Order evidence and security review.

### Automation

- Consider an API contract test set that checks required identity context for every protected endpoint.
- Consider a review check that flags protected API operations without an authorization-focused test reference, once endpoint metadata is standardized.

## 11. Regression Evidence

The corrective action is implemented and regression-tested:

- `getTicket_shouldReturnTicketById` proves an authorized employee can view their own ticket.
- `getTicket_shouldRejectMissingIdentityHeaders` proves missing caller context returns `403 FORBIDDEN`.
- `getTicket_shouldRejectEmployeeViewingAnotherEmployeesTicket` proves an employee cannot view another employee's ticket.
- `mvn clean test` passed with 22 tests, 0 failures, 0 errors, and 0 skipped.
- `TicketController` now passes `X-User-Id` and `X-User-Role` into the view use case.
- `TicketService` now applies the documented role/ownership view policy before mapping the ticket response.

GitHub Actions CI was not newly triggered because no commit or push was performed. The existing workflow remains unchanged and runs `mvn clean test` for Pull Requests; a new PR run is required for fresh CI evidence.
