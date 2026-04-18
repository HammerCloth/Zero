# snapshot Specification

## Purpose
TBD - created by archiving change asset-manager. Update Purpose after archive.
## Requirements
### Requirement: Create snapshot
The system SHALL allow users to create a snapshot recording all account balances for a specific date.

#### Scenario: Create new snapshot
- **WHEN** user selects date and enters balances for all active accounts
- **THEN** system creates snapshot record with the date
- **THEN** system creates snapshot_items for each account balance
- **THEN** system records created_by user

#### Scenario: Prevent duplicate snapshot for same date
- **WHEN** user tries to create snapshot for a date that already has a snapshot
- **THEN** system displays warning "该日期已有快照记录"
- **THEN** system offers to edit existing snapshot instead

### Requirement: Copy from previous snapshot
The system SHALL pre-fill new snapshot form with values from the most recent snapshot.

#### Scenario: Auto-fill from previous snapshot
- **WHEN** user opens new snapshot form
- **THEN** system pre-fills all account balances with values from most recent snapshot
- **THEN** user can modify only the accounts that have changed

#### Scenario: First snapshot with no history
- **WHEN** user creates first snapshot (no previous snapshots exist)
- **THEN** system shows empty balance fields (defaulting to 0)

### Requirement: Display balance changes
The system SHALL show the difference between current input and previous snapshot values.

#### Scenario: Show balance change indicator
- **WHEN** user enters balance different from previous snapshot
- **THEN** system displays change amount (e.g., "+5,000" or "-2,000")
- **THEN** positive changes shown in green, negative in red

#### Scenario: Show net worth change
- **WHEN** user is entering snapshot data
- **THEN** system displays real-time calculation of:
  - Current net worth (sum of all assets minus liabilities)
  - Change from previous snapshot (amount and percentage)

### Requirement: Edit snapshot
The system SHALL allow users to edit existing snapshots.

#### Scenario: Edit snapshot balance
- **WHEN** user edits a balance in existing snapshot
- **THEN** system updates the snapshot_item record
- **THEN** system recalculates and displays updated net worth

### Requirement: Delete snapshot
The system SHALL allow users to delete snapshots.

#### Scenario: Delete snapshot with confirmation
- **WHEN** user clicks delete on a snapshot
- **THEN** system displays confirmation dialog
- **WHEN** user confirms deletion
- **THEN** system deletes snapshot and all associated snapshot_items and events

### Requirement: View snapshot history
The system SHALL allow users to view historical snapshots.

#### Scenario: List all snapshots
- **WHEN** user navigates to snapshot history
- **THEN** system displays list of snapshots ordered by date (newest first)
- **THEN** each row shows: date, net worth, change from previous

#### Scenario: View snapshot details
- **WHEN** user clicks on a snapshot in history
- **THEN** system displays all account balances for that snapshot
- **THEN** system displays any events (大事记) associated with that snapshot

### Requirement: Snapshot validation
The system SHALL validate snapshot data before saving.

#### Scenario: Credit accounts stored as negative
- **WHEN** user enters credit card balance as positive number (e.g., 3500)
- **THEN** system stores it as negative (-3500) for net worth calculation
- **THEN** system displays it as "待还 ¥3,500"

#### Scenario: All accounts required
- **WHEN** user tries to save snapshot with missing account balances
- **THEN** system displays error indicating which accounts are missing

