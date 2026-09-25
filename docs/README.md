# Documentation Index

This folder stores project documentation grouped by purpose so it is easier to navigate, review, and maintain.

## Structure

- 01-requirements/
  - business requirements and traceability
- 02-architecture/
  - architecture, domain model, and data model
- 03-api/
  - API contract and external interface documentation
- 04-design/
  - design decisions, coding rules, and technical planning documents
- 05-testing/
  - test strategy, test design, and execution verification
- 06-quality-security/
  - quality gates, AI review, security review, and governance evidence
- 07-operations/
  - release readiness, operational review, and project operations
- 08-incident-learning/
  - RCA, lessons learned, and post-incident improvement evidence
- 09-templates/
  - reusable templates for PRs, RCA, review, and documentation
- knowledge/
  - working knowledge and reusable learning records

## Source of truth

The most important source documents are:

- Requirements-IT-Support-Ticket-Document.md
- Software-requirements.md
- api-spec.md
- architecture.md
- rca.md
- close-out.md

## Naming convention

Use a consistent naming pattern:

- requirements: Requirements-..., requirements-...
- API: api-..., api-spec.md
- architecture: architecture.md, domain-model.md
- testing: test-design.md, testing.md, e2e-...
- quality: owasp-review.md, quality-report.md
- operations: release-readiness.md, close-out.md
- incident: rca.md, lessons-learned.md

## Notes

This structure is intended to improve discoverability. Existing files at the root of the docs folder may remain temporarily during migration until all documents are reclassified into the new folders.
