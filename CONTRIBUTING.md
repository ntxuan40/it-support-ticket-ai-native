# Contributing guide

This project is a small, local-first engineering app. Contributions should stay aligned with the implemented contract and should not add undocumented features or behavior.

## Development setup

Before working on the project:

1. Install Java 22.
2. Install Maven.
3. Install Node.js and npm for the frontend.
4. Clone the repository and open it in your local development environment.
5. Confirm the backend can run from the module directory.

Backend setup:

```bash
cd it-support-ticket-app/backend
mvn clean test
mvn spring-boot:run
```

Frontend setup:

```bash
cd it-support-ticket-app/frontend
npm install
npm run build
```

## Branch naming

Use short, descriptive branch names that reflect the task:

- feature/ticket-creation-flow
- fix/validation-error-messaging
- docs/api-contract-alignment
- chore/frontend-build-cleanup

Avoid vague names such as fix-it or update-files.

## GitHub Issue-first workflow

All work should begin with a GitHub Issue or equivalent task description.

Required workflow:

1. Open or reference an issue.
2. State the problem, expected behavior, and acceptance criteria.
3. Implement only the scoped changes needed for that issue.
4. Update documentation when behavior changes.
5. Close the issue only after verification is complete.

Do not treat undocumented feature work as an accepted change without a tracked issue and a documented requirement.

## Commit convention

Use commits that describe the intent of the change clearly and concisely.

Preferred style:

- feat: add ticket creation workflow
- fix: reject duplicate technician assignment
- docs: align API contract documentation
- test: cover invalid ticket transition
- chore: update frontend asset build config

Keep commits small and logically grouped.

## Tests required

For backend changes:

```bash
cd it-support-ticket-app/backend
mvn test
```

For frontend changes:

```bash
cd it-support-ticket-app/frontend
npm run build
```

Documentation-only changes should still be reviewed against the implementation and should not claim features that are not present.

## PR requirements

Pull requests must include:

- a clear summary of the change
- the exact verification commands used
- the observed result
- links to the related issue or requirement if applicable
- any risks, assumptions, or follow-up actions

PRs should not claim completion without fresh evidence.

## AI / Copilot usage

AI tools may be used to draft code, documentation, tests, and analysis, but the final result must be reviewed by a human.

Required safeguards:

- confirm all generated code matches the repository's actual behavior
- do not invent undocumented endpoints or workflow rules
- do not claim a feature is complete unless it is implemented and verified
- validate assumptions against the backend contract and current tests

AI-generated documentation should be checked against:

- the Java backend implementation
- the frontend API calls
- the SQLite configuration
- the current test results

## Human review requirement

Every change must be reviewed by a human before merge.

At minimum, the reviewer should check:

- the code matches the documented API contract
- the behavior is consistent with the domain rules
- tests are relevant and verifiable
- no runtime SQLite files are accidentally committed
- the docs do not claim unsupported features

## Documentation requirement

Any behavioral or API change requires matching updates to the repository documentation.

Minimum required updates when a feature or contract changes:

- README.md
- CONTRIBUTING.md if contributor workflow changes
- docs/api-spec.md when the API contract changes
- docs/requirements-traceability.md when requirements are affected
- other relevant design or testing docs when behavior changes

The documentation must match the implemented code. If the implementation differs from the spec, the spec must be corrected or the implementation must be revised before merge.

## Definition of done

A change is done only when:

- the code compiles or the relevant tests pass
- the documentation is updated
- the human review is complete
- the final result is consistent with the actual implementation
