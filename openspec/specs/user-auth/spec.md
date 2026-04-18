# user-auth Specification

## Purpose
TBD - created by archiving change asset-manager. Update Purpose after archive.
## Requirements
### Requirement: First-time setup
The system SHALL display an initial setup page when no users exist in the database. The setup page SHALL allow creating the first administrator account with username and password.

#### Scenario: First access shows setup page
- **WHEN** user accesses the system for the first time (no users in database)
- **THEN** system displays the initial setup page with username and password fields

#### Scenario: Create admin account
- **WHEN** user submits valid username and password on setup page
- **THEN** system creates an administrator account with the provided credentials
- **THEN** system redirects to login page

#### Scenario: Setup page not accessible after admin exists
- **WHEN** user tries to access setup page after an admin account exists
- **THEN** system redirects to login page

### Requirement: User login
The system SHALL allow users to login with username and password. The system SHALL issue JWT tokens upon successful authentication.

#### Scenario: Successful login
- **WHEN** user submits correct username and password
- **THEN** system returns an access token (1 hour validity)
- **THEN** system sets a refresh token in HttpOnly cookie (30 days validity)
- **THEN** system redirects to dashboard

#### Scenario: Failed login with wrong password
- **WHEN** user submits incorrect password
- **THEN** system displays error message "用户名或密码错误"
- **THEN** system increments failed login counter

#### Scenario: Remember me option
- **WHEN** user checks "记住我" and logs in successfully
- **THEN** refresh token cookie validity extends to 30 days
- **THEN** user remains logged in across browser sessions

### Requirement: Login rate limiting
The system SHALL limit failed login attempts to prevent brute force attacks.

#### Scenario: Account lockout after failed attempts
- **WHEN** user fails login 5 times within 15 minutes for same IP
- **THEN** system blocks login attempts from that IP for 15 minutes
- **THEN** system displays message "登录尝试次数过多，请15分钟后再试"

### Requirement: Token refresh
The system SHALL automatically refresh access tokens using the refresh token.

#### Scenario: Access token expired
- **WHEN** access token expires and valid refresh token exists
- **THEN** system automatically issues new access token
- **THEN** user session continues without interruption

#### Scenario: Refresh token expired
- **WHEN** refresh token expires
- **THEN** system redirects user to login page

### Requirement: User logout
The system SHALL allow users to logout and invalidate their session.

#### Scenario: User logs out
- **WHEN** user clicks logout button
- **THEN** system clears refresh token cookie
- **THEN** system redirects to login page

### Requirement: Password change
The system SHALL allow users to change their password.

#### Scenario: Change password successfully
- **WHEN** logged-in user submits current password and new password
- **THEN** system verifies current password
- **THEN** system updates password hash in database
- **THEN** system displays success message

#### Scenario: Force password change on first login
- **WHEN** user with must_change_password flag logs in
- **THEN** system redirects to password change page
- **THEN** user cannot access other pages until password is changed

### Requirement: Admin user management
The system SHALL allow admin users to manage other users.

#### Scenario: Admin adds new user
- **WHEN** admin submits new username and temporary password
- **THEN** system creates user with must_change_password flag set to true
- **THEN** admin can share temporary password with new user

#### Scenario: Admin resets user password
- **WHEN** admin resets another user's password
- **THEN** system sets new temporary password
- **THEN** system sets must_change_password flag to true

### Requirement: Password security
The system SHALL store passwords securely using bcrypt hashing.

#### Scenario: Password stored as hash
- **WHEN** user creates or changes password
- **THEN** system stores bcrypt hash (cost=12) instead of plaintext
- **THEN** original password is never stored or logged

#### Scenario: Password minimum length
- **WHEN** user sets password shorter than 8 characters
- **THEN** system rejects with error "密码至少需要8个字符"

