# Release notes

## Version

1.0.0-SNAPSHOT

## Summary

This release delivers the initial MVP of an internal IT support ticket workflow with a Java 22 backend, SQLite persistence, and a lightweight Vite frontend.

## Included features

- ticket creation
- ticket retrieval by ID
- assignment to one technician
- work start by assigned technician
- ticket resolution with note requirement
- validation and business-rule enforcement
- SQLite-backed persistence
- seeded demo data when the database is empty

## Non-goals

- enterprise identity integration
- full ticket search/filtering
- SLA automation
- attachments or comments
- external notification channels

## Verification

The current backend evidence in the repository shows:

- 11 tests run
- 0 failures
- 0 errors
- 0 skipped

## Deployment note

This is intended for local demonstration and controlled development use. It is not intended to replace a production support-platform environment.
