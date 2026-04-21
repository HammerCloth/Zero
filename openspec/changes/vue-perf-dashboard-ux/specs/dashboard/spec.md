## MODIFIED Requirements

### Requirement: Mobile responsive layout

The system SHALL adapt dashboard layout for mobile devices.

#### Scenario: Mobile view

- **WHEN** user views dashboard on mobile device (width < 768px)
- **THEN** charts display in single column layout
- **THEN** charts support horizontal scroll for timeline views
- **THEN** pie charts remain fully visible
- **THEN** the two asset composition cards (by type and by owner) SHALL NOT be forced into two side-by-side columns narrower than the breakpoint; they SHALL stack vertically so each chart has usable width
- **THEN** composition pie charts SHALL show legend or labels in a scrollable or wrapped configuration so category names are not clipped by the card boundary

## ADDED Requirements

### Requirement: Account share within each asset type

The system SHALL display a chart or equivalent visualization showing, for the **latest snapshot** used for composition, how **each account** contributes **within each asset type** (proportions that sum to 100% per type among accounts with non-zero balance in that type, excluding types with no accounts). Labels for account names and types SHALL use settings-store labels where applicable.

#### Scenario: Type with multiple accounts

- **WHEN** user views dashboard and at least one asset type has two or more accounts with non-zero balance in the latest snapshot
- **THEN** the visualization shows each account's relative share within that type

#### Scenario: Type with single account

- **WHEN** an asset type has exactly one account with balance
- **THEN** that type still appears with 100% attributed to that account or an equivalent clear display

#### Scenario: No snapshot

- **WHEN** user has no snapshots
- **THEN** the visualization shows empty state consistent with other dashboard charts without throwing
