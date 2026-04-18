package repository

import (
	"database/sql"
	"fmt"
	"time"

	"zero/backend/internal/model"
)

type UserRepository struct {
	db *sql.DB
}

func NewUserRepository(db *sql.DB) *UserRepository {
	return &UserRepository{db: db}
}

func (r *UserRepository) CountUsers() (int, error) {
	var count int
	err := r.db.QueryRow("SELECT COUNT(1) FROM users").Scan(&count)
	if err != nil {
		return 0, fmt.Errorf("count users: %w", err)
	}
	return count, nil
}

func (r *UserRepository) Create(user *model.User) error {
	_, err := r.db.Exec(`
		INSERT INTO users(id, username, password_hash, is_admin, must_change_password)
		VALUES(?, ?, ?, ?, ?)
	`, user.ID, user.Username, user.PasswordHash, boolToInt(user.IsAdmin), boolToInt(user.MustChangePassword))
	if err != nil {
		return fmt.Errorf("create user: %w", err)
	}
	return nil
}

func (r *UserRepository) GetByID(id string) (*model.User, error) {
	row := r.db.QueryRow(`
		SELECT id, username, password_hash, is_admin, must_change_password, created_at
		FROM users WHERE id = ?
	`, id)
	return scanUser(row)
}

func (r *UserRepository) GetByUsername(username string) (*model.User, error) {
	row := r.db.QueryRow(`
		SELECT id, username, password_hash, is_admin, must_change_password, created_at
		FROM users WHERE username = ?
	`, username)
	return scanUser(row)
}

func (r *UserRepository) List() ([]model.User, error) {
	rows, err := r.db.Query(`
		SELECT id, username, password_hash, is_admin, must_change_password, created_at
		FROM users
		ORDER BY created_at ASC
	`)
	if err != nil {
		return nil, fmt.Errorf("list users: %w", err)
	}
	defer rows.Close()

	var users []model.User
	for rows.Next() {
		var user model.User
		var isAdmin, mustChange int
		var createdAt string

		if err := rows.Scan(
			&user.ID,
			&user.Username,
			&user.PasswordHash,
			&isAdmin,
			&mustChange,
			&createdAt,
		); err != nil {
			return nil, fmt.Errorf("scan user row: %w", err)
		}

		user.IsAdmin = intToBool(isAdmin)
		user.MustChangePassword = intToBool(mustChange)
		user.CreatedAt = parseSQLiteTime(createdAt)
		users = append(users, user)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate user rows: %w", err)
	}

	return users, nil
}

func (r *UserRepository) UpdatePassword(userID, passwordHash string, mustChange bool) error {
	_, err := r.db.Exec(`
		UPDATE users
		SET password_hash = ?, must_change_password = ?
		WHERE id = ?
	`, passwordHash, boolToInt(mustChange), userID)
	if err != nil {
		return fmt.Errorf("update user password: %w", err)
	}
	return nil
}

func scanUser(row *sql.Row) (*model.User, error) {
	var user model.User
	var isAdmin, mustChange int
	var createdAt string

	if err := row.Scan(
		&user.ID,
		&user.Username,
		&user.PasswordHash,
		&isAdmin,
		&mustChange,
		&createdAt,
	); err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("scan user: %w", err)
	}

	user.IsAdmin = intToBool(isAdmin)
	user.MustChangePassword = intToBool(mustChange)
	user.CreatedAt = parseSQLiteTime(createdAt)

	return &user, nil
}

func boolToInt(v bool) int {
	if v {
		return 1
	}
	return 0
}

func intToBool(v int) bool {
	return v == 1
}

func parseSQLiteTime(value string) time.Time {
	layouts := []string{
		time.RFC3339,
		"2006-01-02 15:04:05",
		"2006-01-02T15:04:05Z07:00",
	}
	for _, layout := range layouts {
		t, err := time.Parse(layout, value)
		if err == nil {
			return t
		}
	}
	return time.Time{}
}
