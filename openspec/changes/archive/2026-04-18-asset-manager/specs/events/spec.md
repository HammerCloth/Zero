## ADDED Requirements

### Requirement: Event categories
The system SHALL support the following event categories: rent (房租), travel (旅行), medical (医疗), appliance (家电装修), social (人情往来), other (其他).

#### Scenario: Create event with valid category
- **WHEN** user creates event with category "travel"
- **THEN** system creates event with category set to "travel"

### Requirement: Add event to snapshot
The system SHALL allow users to add events (大事记) when creating or editing a snapshot.

#### Scenario: Add event during snapshot creation
- **WHEN** user clicks "添加大事记" on snapshot form
- **THEN** system displays event form with category, description, and amount fields
- **WHEN** user fills and submits event
- **THEN** system creates event linked to the snapshot

#### Scenario: Add multiple events
- **WHEN** user adds multiple events to same snapshot
- **THEN** system creates all events linked to the same snapshot
- **THEN** snapshot form displays list of added events

### Requirement: Event amount handling
The system SHALL store event amounts as negative numbers for expenses.

#### Scenario: Expense stored as negative
- **WHEN** user enters event amount as 18000 for travel expense
- **THEN** system stores amount as -18000
- **THEN** system displays as "-¥18,000"

### Requirement: Edit and delete events
The system SHALL allow users to edit and delete events.

#### Scenario: Edit event
- **WHEN** user edits event description or amount
- **THEN** system updates the event record

#### Scenario: Delete event
- **WHEN** user deletes an event
- **THEN** system removes the event record
- **THEN** snapshot remains unchanged

### Requirement: View events in snapshot
The system SHALL display events when viewing snapshot details.

#### Scenario: Show events in snapshot view
- **WHEN** user views snapshot details
- **THEN** system displays list of events for that snapshot
- **THEN** each event shows: category icon, description, amount

### Requirement: Explain net worth change
The system SHALL use events to explain significant net worth changes.

#### Scenario: Display event summary in snapshot
- **WHEN** snapshot has events totaling -33000
- **WHEN** net worth change is -12000
- **THEN** system displays breakdown:
  - "大额支出: -¥33,000"
  - "实际增长: +¥21,000"

### Requirement: Annual event statistics
The system SHALL provide annual statistics for events by category.

#### Scenario: View annual event summary
- **WHEN** user requests annual event statistics for 2024
- **THEN** system displays total amount per category:
  - 房租: ¥60,000 (4次)
  - 旅行: ¥45,000 (3次)
  - etc.
- **THEN** system displays grand total of all events
