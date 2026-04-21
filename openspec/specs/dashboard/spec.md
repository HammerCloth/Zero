# dashboard Specification

## Purpose
TBD - created by archiving change asset-manager. Update Purpose after archive.
## Requirements
### Requirement: Key metrics cards
The system SHALL display key financial metrics at the top of the dashboard.

#### Scenario: Display current net worth
- **WHEN** user views dashboard
- **THEN** system displays current net worth from most recent snapshot

#### Scenario: Display monthly change
- **WHEN** user views dashboard
- **THEN** system displays change from previous snapshot (amount and percentage)
- **THEN** positive change shown in green with ▲, negative in red with ▼

#### Scenario: Display annual change
- **WHEN** user views dashboard
- **THEN** system calculates and displays change since start of year
- **THEN** system displays both amount and percentage

#### Scenario: Display annualized return
- **WHEN** user has at least 2 snapshots spanning different months
- **THEN** system calculates and displays annualized return rate

### Requirement: Net worth trend chart
The system SHALL display a line chart showing net worth over time.

#### Scenario: Display trend chart
- **WHEN** user views dashboard
- **THEN** system displays line chart with:
  - X-axis: dates
  - Y-axis: net worth amount
  - Data points for each snapshot

#### Scenario: Time range selection
- **WHEN** user selects time range (近3月/近6月/近1年/近2年/全部)
- **THEN** chart updates to show only snapshots within selected range

#### Scenario: Hover interaction
- **WHEN** user hovers over a point on the chart
- **THEN** tooltip displays: date, net worth, change from previous

### Requirement: Asset composition pie charts
The system SHALL display pie charts showing asset breakdown.

#### Scenario: Display by asset type
- **WHEN** user views dashboard
- **THEN** system displays pie chart with breakdown by type:
  - 现金, 固收, 基金, 养老, 公积金 (as positive segments)
  - 负债 shown separately or as negative indicator

#### Scenario: Display by owner
- **WHEN** user views dashboard
- **THEN** system displays pie chart with breakdown by owner:
  - A, B, 共同

### Requirement: Asset stacked area chart
The system SHALL display a stacked area chart showing asset composition over time.

#### Scenario: Display stacked chart
- **WHEN** user views dashboard
- **THEN** system displays stacked area chart with:
  - Different colors for each asset type
  - Time on X-axis
  - Cumulative value on Y-axis

### Requirement: Monthly growth bar chart
The system SHALL display a bar chart showing monthly net worth changes.

#### Scenario: Display monthly growth
- **WHEN** user views dashboard
- **THEN** system displays bar chart with:
  - One bar per month
  - Positive bars (green) above axis for growth months
  - Negative bars (red) below axis for decline months

#### Scenario: Identify anomaly months
- **WHEN** a month shows significant decline
- **THEN** user can click bar to see associated events (大事记)

### Requirement: Account detail line chart
The system SHALL display individual account trends.

#### Scenario: Display account trends
- **WHEN** user views dashboard
- **THEN** system displays multi-line chart with one line per account

#### Scenario: Filter by account type
- **WHEN** user selects account type filter (现金/固收/基金/公积金/养老)
- **THEN** chart shows only accounts of selected type

### Requirement: Mobile responsive layout
The system SHALL adapt dashboard layout for mobile devices.

#### Scenario: Mobile view
- **WHEN** user views dashboard on mobile device (width < 768px)
- **THEN** charts display in single column layout
- **THEN** charts support horizontal scroll for timeline views
- **THEN** pie charts remain fully visible
- **THEN** the two asset composition cards (by type and by owner) SHALL NOT be forced into two side-by-side columns narrower than the breakpoint; they SHALL stack vertically so each chart has usable width
- **THEN** composition pie charts SHALL show legend or labels in a scrollable or wrapped configuration so category names are not clipped by the card boundary

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

### Requirement: Data export
The system SHALL allow users to export dashboard data.

#### Scenario: Export chart as image
- **WHEN** user clicks export button on a chart
- **THEN** system downloads chart as PNG image

#### Scenario: Export data as CSV
- **WHEN** user clicks "导出数据" button
- **THEN** system downloads CSV file with all snapshot data

