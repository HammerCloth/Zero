# automated-testing Specification

## Purpose
TBD - created by archiving change add-unit-tests. Update Purpose after archive.
## Requirements
### Requirement: Backend unit tests are runnable via standard Go tooling

The system SHALL provide automated tests for the Go backend such that `go test ./...` from the `backend` module completes successfully and exercises core domain logic without requiring a running Docker stack.

#### Scenario: Test command succeeds

- **WHEN** a developer runs `go test ./...` from the `zero/backend` module directory
- **THEN** all packages compile and tests pass (exit code 0)

#### Scenario: Core calculations are covered

- **WHEN** tests run for packages under `internal/service` (or equivalent)
- **THEN** at least net-worth aggregation and item/event normalization behaviors that are implemented as testable functions SHALL have table-driven or equivalent tests that assert expected outputs for representative inputs

### Requirement: Frontend unit tests are runnable via npm

The system SHALL provide a documented npm script (e.g. `npm test`) in the frontend project that executes unit tests for pure utilities without starting the full production server.

#### Scenario: Test script succeeds

- **WHEN** a developer runs the documented test script from the `zero/frontend` directory after installing dependencies
- **THEN** the test runner completes successfully (exit code 0)

#### Scenario: Formatting utilities are covered

- **WHEN** tests run for shared formatting or parsing helpers used by the UI
- **THEN** at least one representative scenario SHALL assert stable output for a given numeric or string input

### Requirement: Time-sensitive dashboard logic is testable without flaking

The system SHALL avoid non-deterministic failures in automated tests caused by the wall clock. Dashboard or summary logic that depends on "today" or "current year" SHALL be structured so tests can use fixed reference times or injected clocks.

#### Scenario: Fixed reference time

- **WHEN** automated tests exercise date-boundary behavior for dashboard or summary calculations
- **THEN** those tests use a fixed reference instant or injected time provider such that the same test produces the same result on different calendar days

### Requirement: Documentation lists test commands

The project SHALL document how to run backend and frontend unit tests in an existing developer-facing document (e.g. repository or frontend README), including the exact commands and prerequisite steps (e.g. `npm ci`).

#### Scenario: Developer finds commands

- **WHEN** a new contributor reads the documented testing section
- **THEN** they can run backend and frontend unit tests using only the documented commands without undocumented environment variables

