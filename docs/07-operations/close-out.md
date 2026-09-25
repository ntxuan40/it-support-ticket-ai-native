# Close-out Report

## 1. Work scope and requirement coverage

This close-out covers the confirmed defect in the ticket-view authorization path and the minimal corrective action required to align the implementation with the project requirement baseline.

Direct requirement coverage:

- Requirement 5: role-based action control — the backend validates the acting user's role and identity, and the frontend sends `X-User-Id` and `X-User-Role` headers to authorize each backend action.
- Requirement 6: validation and business errors — authorization failures return structured HTTP errors instead of disclosing protected resources.
- API-02: get ticket by ID — the endpoint requires caller identity and role headers, valid ticket lookup, and an authorized actor before returning the ticket.
- Evidence baseline: [Requirements-IT-Support-Ticket-Document.md](Requirements-IT-Support-Ticket-Document.md), [api-spec.md](api-spec.md), and [rca.md](rca.md).

In-scope changes were intentionally limited to the view authorization defect:

- [it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/api/TicketController.java](../it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/api/TicketController.java)
- [it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/application/service/TicketService.java](../it-support-ticket-app/backend/src/main/java/com/example/itsupportticket/application/service/TicketService.java)
- [it-support-ticket-app/backend/src/test/java/com/example/itsupportticket/api/TicketControllerIntegrationTest.java](../it-support-ticket-app/backend/src/test/java/com/example/itsupportticket/api/TicketControllerIntegrationTest.java)

## 2. Defect summary

The confirmed defect was a broken authorization boundary in the GET ticket path:

- `GET /api/tickets/{id}` accepted only the ticket ID.
- The controller did not consume `X-User-Id` and `X-User-Role` for the view operation.
- The service looked up the ticket and returned it without checking whether the caller was allowed to view it.
- The previous tests validated successful retrieval and not-found behavior, but not unauthorized access or missing identity headers.

The root cause and corrective logic are documented in [rca.md](rca.md).

## 3. Corrective action taken

The fix was deliberately narrow and aligned to the requirement contract:

1. The view endpoint now receives the acting identity headers from the HTTP request.
2. The controller passes the actor identity and role into the ticket-view use case.
3. The service validates the required identity values before the view is processed.
4. The service checks the allowed-view policy before returning the ticket:
   - ADMIN and TECH_LEAD can view any ticket.
   - EMPLOYEE may view only tickets created by that employee.
   - IT_TECHNICIAN may view only tickets assigned to that technician.
5. Unauthorized or missing-header requests return the structured forbidden error instead of disclosing the resource.

This preserves the existing not-found behavior while closing the authorization gap.

## 4. Regression tests added

The regression suite now exercises the documented negative cases:

- unauthorized employee viewing another employee's ticket
- missing identity headers on the view request
- allowed employee viewing their own ticket
- standard not-found response for a missing ticket

The relevant scenarios are in [TicketControllerIntegrationTest.java](../it-support-ticket-app/backend/src/test/java/com/example/itsupportticket/api/TicketControllerIntegrationTest.java).

## 5. Verification evidence

Fresh verification performed in this environment:

- Test command: `mvn -Dtest=TicketControllerIntegrationTest test`
- Evidence report: [it-support-ticket-app/backend/target/surefire-reports/TEST-com.example.itsupportticket.api.TicketControllerIntegrationTest.xml](../it-support-ticket-app/backend/target/surefire-reports/TEST-com.example.itsupportticket.api.TicketControllerIntegrationTest.xml)

Verified result from the generated report:

- tests: 19
- failures: 0
- errors: 0
- skipped: 0

This is the evidence that the corrected ticket-view authorization behavior and adjacent API flows are currently passing in the local project environment.

## 6. Security, quality, and governance review

- Security review outcome: the requirement mismatch was confirmed as a broken access-control issue and was fixed without broadening scope.
- AI governance: this work remained scoped to the documented defect and was reviewed against the requirement baseline before closure.
- Quality gate: the fix is tied to explicit negative tests and not only to happy-path success.
- CI note: GitHub Actions was not triggered by a real push or pull request in this environment, so the repository remote workflow remains unverified from a branch run. No false PASS claim is made for GitHub-hosted CI.

## 7. Definition of done

The defect is considered closed for this work item when all of the following are true:

- requirement trace is aligned to the implementation
- authorized and unauthorized view flows are covered by automated tests
- missing identity headers are rejected
- unauthorized access is denied with a stable structured error
- the local regression suite passes with fresh evidence
- no unrelated production code was changed

This work item meets that definition based on the evidence above.

## 8. Final statement

The fix for the ticket-view authorization defect is implemented, regression-tested, and locally verified. No commit or push was performed, and no GitHub Actions PASS claim is made beyond the local Maven evidence captured in this workspace.
