# account-management Specification

## Purpose
TBD - created by archiving change asset-manager. Update Purpose after archive.
## Requirements
### Requirement: Account types
The system SHALL support the following account types: cash (现金), deposit (固收), fund (基金), pension (养老), housing_fund (公积金), credit (负债).

#### Scenario: Create account with valid type
- **WHEN** user creates account with type "cash"
- **THEN** system creates account with type set to "cash"

#### Scenario: Reject invalid account type
- **WHEN** user tries to create account with invalid type
- **THEN** system rejects with error message

### Requirement: Account ownership
The system SHALL support account ownership: A, B, or shared (共同).

#### Scenario: Create account with ownership
- **WHEN** user creates account with owner "A"
- **THEN** system creates account with owner field set to "A"

#### Scenario: Filter accounts by owner
- **WHEN** user requests accounts filtered by owner "shared"
- **THEN** system returns only accounts with owner "shared"

### Requirement: Account CRUD operations
The system SHALL allow creating, reading, updating, and deactivating accounts.

#### Scenario: Create new account
- **WHEN** user submits account name, type, and owner
- **THEN** system creates account with provided values
- **THEN** system sets is_active to true by default

#### Scenario: List all accounts
- **WHEN** user requests account list
- **THEN** system returns all active accounts sorted by sort_order

#### Scenario: Update account name
- **WHEN** user updates account name
- **THEN** system updates the account name in database

#### Scenario: Deactivate account
- **WHEN** user deactivates an account
- **THEN** system sets is_active to false
- **THEN** account no longer appears in active account list
- **THEN** historical snapshot data for this account is preserved

### Requirement: Account sorting
The system SHALL allow users to customize account display order.

#### Scenario: Change account order
- **WHEN** user drags account to new position
- **THEN** system updates sort_order for affected accounts
- **THEN** account list reflects new order

### Requirement: Default accounts initialization
The system SHALL create default accounts on first user setup based on predefined list.

#### Scenario: Initialize default accounts
- **WHEN** admin account is created during first-time setup
- **THEN** system creates 11 default accounts:
  - A工资卡 (cash, A)
  - B工资卡 (cash, B)
  - 小荷包 (cash, shared)
  - 招行存单 (deposit, shared)
  - 月月宝 (deposit, shared)
  - 国内标普QDII (fund, A)
  - 海外基金 (fund, B)
  - 个人养老金 (pension, A)
  - A住房公积金 (housing_fund, A)
  - B住房公积金 (housing_fund, B)
  - 信用卡 (credit, A)

