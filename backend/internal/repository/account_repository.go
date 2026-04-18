package repository

import (
	"database/sql"
	"fmt"

	"zero/backend/internal/model"
)

type AccountRepository struct {
	db *sql.DB
}

func NewAccountRepository(db *sql.DB) *AccountRepository {
	return &AccountRepository{db: db}
}

func (r *AccountRepository) Create(account *model.Account) error {
	_, err := r.db.Exec(`
		INSERT INTO accounts(id, user_id, name, type, owner, sort_order, is_active)
		VALUES(?, ?, ?, ?, ?, ?, ?)
	`,
		account.ID,
		account.UserID,
		account.Name,
		account.Type,
		account.Owner,
		account.SortOrder,
		boolToInt(account.IsActive),
	)
	if err != nil {
		return fmt.Errorf("create account: %w", err)
	}
	return nil
}

func (r *AccountRepository) ListActive(userID string) ([]model.Account, error) {
	rows, err := r.db.Query(`
		SELECT id, user_id, name, type, owner, sort_order, is_active, created_at
		FROM accounts
		WHERE user_id = ? AND is_active = 1
		ORDER BY sort_order ASC, created_at ASC
	`, userID)
	if err != nil {
		return nil, fmt.Errorf("list active accounts: %w", err)
	}
	defer rows.Close()

	return scanAccounts(rows)
}

func (r *AccountRepository) ListAll(userID string) ([]model.Account, error) {
	rows, err := r.db.Query(`
		SELECT id, user_id, name, type, owner, sort_order, is_active, created_at
		FROM accounts
		WHERE user_id = ?
		ORDER BY sort_order ASC, created_at ASC
	`, userID)
	if err != nil {
		return nil, fmt.Errorf("list all accounts: %w", err)
	}
	defer rows.Close()

	return scanAccounts(rows)
}

func (r *AccountRepository) GetByID(id string) (*model.Account, error) {
	var account model.Account
	var isActive int
	var createdAt string

	err := r.db.QueryRow(`
		SELECT id, user_id, name, type, owner, sort_order, is_active, created_at
		FROM accounts
		WHERE id = ?
	`, id).Scan(
		&account.ID,
		&account.UserID,
		&account.Name,
		&account.Type,
		&account.Owner,
		&account.SortOrder,
		&isActive,
		&createdAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("get account by id: %w", err)
	}

	account.IsActive = intToBool(isActive)
	account.CreatedAt = parseSQLiteTime(createdAt)
	return &account, nil
}

func (r *AccountRepository) Update(account *model.Account) error {
	_, err := r.db.Exec(`
		UPDATE accounts
		SET name = ?, type = ?, owner = ?, sort_order = ?
		WHERE id = ?
	`, account.Name, account.Type, account.Owner, account.SortOrder, account.ID)
	if err != nil {
		return fmt.Errorf("update account: %w", err)
	}
	return nil
}

func (r *AccountRepository) Deactivate(id string) error {
	_, err := r.db.Exec(`UPDATE accounts SET is_active = 0 WHERE id = ?`, id)
	if err != nil {
		return fmt.Errorf("deactivate account: %w", err)
	}
	return nil
}

func (r *AccountRepository) Reorder(userID string, accountIDs []string) error {
	tx, err := r.db.Begin()
	if err != nil {
		return fmt.Errorf("begin reorder accounts: %w", err)
	}

	for idx, accountID := range accountIDs {
		if _, err := tx.Exec(`
			UPDATE accounts
			SET sort_order = ?
			WHERE id = ? AND user_id = ?
		`, idx+1, accountID, userID); err != nil {
			_ = tx.Rollback()
			return fmt.Errorf("update account sort order: %w", err)
		}
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("commit reorder accounts: %w", err)
	}
	return nil
}

func scanAccounts(rows *sql.Rows) ([]model.Account, error) {
	var accounts []model.Account

	for rows.Next() {
		var account model.Account
		var isActive int
		var createdAt string

		if err := rows.Scan(
			&account.ID,
			&account.UserID,
			&account.Name,
			&account.Type,
			&account.Owner,
			&account.SortOrder,
			&isActive,
			&createdAt,
		); err != nil {
			return nil, fmt.Errorf("scan account row: %w", err)
		}

		account.IsActive = intToBool(isActive)
		account.CreatedAt = parseSQLiteTime(createdAt)
		accounts = append(accounts, account)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate account rows: %w", err)
	}

	return accounts, nil
}
