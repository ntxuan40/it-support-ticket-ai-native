# AI-Assisted Development, Human-Controlled Delivery

## Purpose

This document defines the governance process for using AI assistance while keeping requirements validation, technical review, security review, and merge approval under human control.

AI-generated output is a proposal. It is not approved merely because it compiles or because generated tests pass.

## AI May Assist With

AI may assist with:

- Analyzing requirements.
- Generating implementation candidates.
- Generating focused tests.
- Identifying coverage gaps.
- Reviewing possible risks.
- Generating technical documentation.

AI output must remain traceable to the repository requirements and must be reviewed before acceptance.

## Human Responsibilities

A human reviewer must:

- Validate the requirements and acceptance criteria.
- Review the generated diff.
- Review generated or modified tests.
- Independently review security implications.
- Verify observable business behavior.
- Review the CI result and required checks.
- Approve or reject the merge.

The human reviewer owns the final engineering decision.

## Review Process

1. **Establish the source of truth**
   - Read the applicable requirements and acceptance criteria.
   - Identify the requested scope and explicit out-of-scope behavior.
   - Challenge any behavior that is not supported by the requirements.

2. **Review the proposed implementation**
   - Inspect the complete diff, not only the AI summary.
   - Check correctness, maintainability, error handling, and API behavior.
   - Confirm that no business requirement, API behavior, schema change, or dependency was invented without approval.

3. **Review the proposed tests**
   - Map tests to requirements and acceptance criteria.
   - Check positive and negative scenarios.
   - Check authorization and identity boundaries.
   - Check valid and invalid state transitions.
   - Check persistence and test isolation.
   - Look for tests that can pass while production behavior is still wrong.

4. **Perform an independent security review**
   - Review authorization and access control.
   - Review input validation and error handling.
   - Check for secrets and sensitive data exposure.
   - Review dependency and CI/CD risks.
   - Do not treat passing tests as proof of security.

5. **Verify CI and delivery controls**
   - Confirm tests pass in CI.
   - Confirm required status checks pass.
   - Confirm no unexpected workflow or configuration changes were introduced.
   - Confirm branch protection and approval requirements are applied.

6. **Make the human decision**
   - Approve only when the requirements, code, tests, security review, and CI evidence are acceptable.
   - Reject or request changes when evidence is incomplete or behavior is unsupported.

## Human Review Checklist

### Requirement Review

- [ ] Does the implementation match the specification?
- [ ] Are all acceptance criteria addressed?
- [ ] Has any business behavior been invented?
- [ ] Are out-of-scope features excluded?

### Code Review

- [ ] Is the implementation correct for the documented behavior?
- [ ] Is the code maintainable and consistent with the repository?
- [ ] Are errors handled safely and predictably?
- [ ] Does the API behavior match the documented contract?
- [ ] Are unrelated files and behaviors unchanged?

### Test Review

- [ ] Do tests trace to requirements?
- [ ] Are important happy paths covered?
- [ ] Are negative validation cases covered?
- [ ] Are unauthorized actions covered?
- [ ] Are valid and invalid state transitions covered?
- [ ] Is persistence behavior verified where required?
- [ ] Is test data isolated from runtime data?
- [ ] Could a test pass while the production behavior is still incorrect?

### Security Review

- [ ] Is authorization enforced at the backend boundary?
- [ ] Is input validated before state changes?
- [ ] Are secrets absent from code, configuration, and logs?
- [ ] Is sensitive data exposure considered?
- [ ] Are dependency and CI/CD risks reviewed?

### CI Review

- [ ] Do automated tests pass locally and in CI?
- [ ] Do required status checks pass?
- [ ] Is failure propagation working?
- [ ] Are there no unexpected workflow changes?
- [ ] Is the CI result appropriate for the proposed change?

### Human Approval

- [ ] A human reviewed the diff.
- [ ] A human reviewed the generated tests.
- [ ] A human independently reviewed security implications.
- [ ] A human verified the relevant business behavior.
- [ ] A human made the approve/reject merge decision.

## AI Trust Rules

1. AI output is a proposal, not an approval.
2. A human reviews the complete diff before acceptance.
3. Tests must verify observable behavior rather than only implementation details.
4. Security must be reviewed independently from generated test results.
5. Requirements remain the source of truth.
6. Merge requires an explicit human decision.

## Review Evidence

A review record should retain, as applicable:

- The requirement or issue reference.
- The reviewed diff.
- Test commands and results.
- CI run and required-check result.
- Security review findings and disposition.
- Human reviewer decision.

Passing generated tests is evidence of the tested scenarios only. It is not evidence that all requirements, security properties, or business rules are correct.

## Governance Outcome

AI assistance can accelerate analysis, implementation proposals, tests, and documentation. Delivery remains human-controlled because requirements validation, code review, test challenge, security review, CI verification, and merge approval are explicit human responsibilities.
