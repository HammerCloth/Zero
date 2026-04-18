## ADDED Requirements

### Requirement: RESTful API structure
The system SHALL expose a RESTful API for all operations.

#### Scenario: API base path
- **WHEN** client makes API request
- **THEN** all endpoints are under /api/v1/ prefix

### Requirement: Authentication endpoints
The system SHALL provide authentication-related API endpoints.

#### Scenario: POST /api/v1/auth/setup
- **WHEN** client POSTs {username, password} to /api/v1/auth/setup
- **WHEN** no users exist in database
- **THEN** system creates admin user and returns {user, access_token}

#### Scenario: POST /api/v1/auth/login
- **WHEN** client POSTs {username, password, remember_me} to /api/v1/auth/login
- **THEN** system validates credentials
- **THEN** system returns {user, access_token} with refresh_token in HttpOnly cookie

#### Scenario: POST /api/v1/auth/refresh
- **WHEN** client POSTs to /api/v1/auth/refresh with valid refresh_token cookie
- **THEN** system returns new {access_token}

#### Scenario: POST /api/v1/auth/logout
- **WHEN** client POSTs to /api/v1/auth/logout
- **THEN** system clears refresh_token cookie

#### Scenario: GET /api/v1/auth/me
- **WHEN** authenticated client GETs /api/v1/auth/me
- **THEN** system returns current user info {id, username, is_admin}

### Requirement: User management endpoints
The system SHALL provide user management API endpoints for admins.

#### Scenario: GET /api/v1/users
- **WHEN** admin GETs /api/v1/users
- **THEN** system returns list of all users

#### Scenario: POST /api/v1/users
- **WHEN** admin POSTs {username, password} to /api/v1/users
- **THEN** system creates new user with must_change_password=true

#### Scenario: PUT /api/v1/users/:id/password
- **WHEN** admin PUTs {password} to /api/v1/users/:id/password
- **THEN** system resets user password with must_change_password=true

#### Scenario: PUT /api/v1/auth/password
- **WHEN** authenticated user PUTs {current_password, new_password}
- **THEN** system changes user's own password

### Requirement: Account endpoints
The system SHALL provide account management API endpoints.

#### Scenario: GET /api/v1/accounts
- **WHEN** authenticated client GETs /api/v1/accounts
- **THEN** system returns all active accounts sorted by sort_order

#### Scenario: POST /api/v1/accounts
- **WHEN** authenticated client POSTs {name, type, owner}
- **THEN** system creates new account

#### Scenario: PUT /api/v1/accounts/:id
- **WHEN** authenticated client PUTs {name, type, owner, sort_order}
- **THEN** system updates account

#### Scenario: DELETE /api/v1/accounts/:id
- **WHEN** authenticated client DELETEs account
- **THEN** system sets is_active=false (soft delete)

#### Scenario: PUT /api/v1/accounts/reorder
- **WHEN** authenticated client PUTs {account_ids: [ordered list]}
- **THEN** system updates sort_order for all accounts

### Requirement: Snapshot endpoints
The system SHALL provide snapshot API endpoints.

#### Scenario: GET /api/v1/snapshots
- **WHEN** authenticated client GETs /api/v1/snapshots
- **THEN** system returns list of snapshots with net_worth calculated
- **THEN** results ordered by date descending

#### Scenario: GET /api/v1/snapshots/:id
- **WHEN** authenticated client GETs /api/v1/snapshots/:id
- **THEN** system returns snapshot with all items and events

#### Scenario: GET /api/v1/snapshots/latest
- **WHEN** authenticated client GETs /api/v1/snapshots/latest
- **THEN** system returns most recent snapshot with all items

#### Scenario: POST /api/v1/snapshots
- **WHEN** authenticated client POSTs {date, items: [{account_id, balance}], events: [{category, description, amount}], note}
- **THEN** system creates snapshot with items and events

#### Scenario: PUT /api/v1/snapshots/:id
- **WHEN** authenticated client PUTs snapshot data
- **THEN** system updates snapshot, items, and events

#### Scenario: DELETE /api/v1/snapshots/:id
- **WHEN** authenticated client DELETEs snapshot
- **THEN** system deletes snapshot, items, and events

### Requirement: Dashboard endpoints
The system SHALL provide dashboard data API endpoints.

#### Scenario: GET /api/v1/dashboard/summary
- **WHEN** authenticated client GETs /api/v1/dashboard/summary
- **THEN** system returns {net_worth, monthly_change, annual_change, annualized_return}

#### Scenario: GET /api/v1/dashboard/trend
- **WHEN** authenticated client GETs /api/v1/dashboard/trend?range=1y
- **THEN** system returns array of {date, net_worth} for chart

#### Scenario: GET /api/v1/dashboard/composition
- **WHEN** authenticated client GETs /api/v1/dashboard/composition
- **THEN** system returns {by_type: {...}, by_owner: {...}} from latest snapshot

#### Scenario: GET /api/v1/dashboard/monthly-growth
- **WHEN** authenticated client GETs /api/v1/dashboard/monthly-growth?year=2024
- **THEN** system returns array of {month, change} for bar chart

### Requirement: Event statistics endpoint
The system SHALL provide event statistics API endpoint.

#### Scenario: GET /api/v1/events/stats
- **WHEN** authenticated client GETs /api/v1/events/stats?year=2024
- **THEN** system returns {by_category: [{category, total, count}], grand_total}

### Requirement: Export endpoint
The system SHALL provide data export API endpoint.

#### Scenario: GET /api/v1/export/csv
- **WHEN** authenticated client GETs /api/v1/export/csv
- **THEN** system returns CSV file with all snapshot data

### Requirement: API error handling
The system SHALL return consistent error responses.

#### Scenario: Authentication error
- **WHEN** unauthenticated client accesses protected endpoint
- **THEN** system returns 401 {error: "未登录或登录已过期"}

#### Scenario: Authorization error
- **WHEN** non-admin client accesses admin endpoint
- **THEN** system returns 403 {error: "无权限执行此操作"}

#### Scenario: Validation error
- **WHEN** client submits invalid data
- **THEN** system returns 400 {error: "...", fields: {...}}

#### Scenario: Not found error
- **WHEN** client requests non-existent resource
- **THEN** system returns 404 {error: "资源不存在"}

### Requirement: CORS configuration
The system SHALL configure CORS to allow frontend access.

#### Scenario: CORS headers
- **WHEN** frontend makes API request
- **THEN** system includes appropriate CORS headers
- **THEN** system allows credentials (for cookies)
