package repository

import (
	"database/sql"
	"fmt"

	"zero/backend/internal/model"
)

type SnapshotRepository struct {
	db *sql.DB
}

func NewSnapshotRepository(db *sql.DB) *SnapshotRepository {
	return &SnapshotRepository{db: db}
}

func (r *SnapshotRepository) Create(snapshot *model.Snapshot) error {
	tx, err := r.db.Begin()
	if err != nil {
		return fmt.Errorf("begin create snapshot: %w", err)
	}

	_, err = tx.Exec(`
		INSERT INTO snapshots(id, user_id, date, note, created_by)
		VALUES(?, ?, ?, ?, ?)
	`, snapshot.ID, snapshot.UserID, snapshot.Date, snapshot.Note, snapshot.CreatedBy)
	if err != nil {
		_ = tx.Rollback()
		return fmt.Errorf("insert snapshot: %w", err)
	}

	for _, item := range snapshot.Items {
		_, err := tx.Exec(`
			INSERT INTO snapshot_items(id, snapshot_id, account_id, balance)
			VALUES(?, ?, ?, ?)
		`, item.ID, snapshot.ID, item.AccountID, item.Balance)
		if err != nil {
			_ = tx.Rollback()
			return fmt.Errorf("insert snapshot item: %w", err)
		}
	}

	for _, event := range snapshot.Events {
		_, err := tx.Exec(`
			INSERT INTO events(id, snapshot_id, category, description, amount)
			VALUES(?, ?, ?, ?, ?)
		`, event.ID, snapshot.ID, event.Category, event.Description, event.Amount)
		if err != nil {
			_ = tx.Rollback()
			return fmt.Errorf("insert snapshot event: %w", err)
		}
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("commit create snapshot: %w", err)
	}
	return nil
}

func (r *SnapshotRepository) Update(snapshot *model.Snapshot) error {
	tx, err := r.db.Begin()
	if err != nil {
		return fmt.Errorf("begin update snapshot: %w", err)
	}

	_, err = tx.Exec(`
		UPDATE snapshots
		SET date = ?, note = ?
		WHERE id = ?
	`, snapshot.Date, snapshot.Note, snapshot.ID)
	if err != nil {
		_ = tx.Rollback()
		return fmt.Errorf("update snapshot: %w", err)
	}

	if _, err := tx.Exec(`DELETE FROM snapshot_items WHERE snapshot_id = ?`, snapshot.ID); err != nil {
		_ = tx.Rollback()
		return fmt.Errorf("clear snapshot items: %w", err)
	}

	if _, err := tx.Exec(`DELETE FROM events WHERE snapshot_id = ?`, snapshot.ID); err != nil {
		_ = tx.Rollback()
		return fmt.Errorf("clear snapshot events: %w", err)
	}

	for _, item := range snapshot.Items {
		if _, err := tx.Exec(`
			INSERT INTO snapshot_items(id, snapshot_id, account_id, balance)
			VALUES(?, ?, ?, ?)
		`, item.ID, snapshot.ID, item.AccountID, item.Balance); err != nil {
			_ = tx.Rollback()
			return fmt.Errorf("reinsert snapshot item: %w", err)
		}
	}

	for _, event := range snapshot.Events {
		if _, err := tx.Exec(`
			INSERT INTO events(id, snapshot_id, category, description, amount)
			VALUES(?, ?, ?, ?, ?)
		`, event.ID, snapshot.ID, event.Category, event.Description, event.Amount); err != nil {
			_ = tx.Rollback()
			return fmt.Errorf("reinsert snapshot event: %w", err)
		}
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("commit update snapshot: %w", err)
	}
	return nil
}

func (r *SnapshotRepository) Delete(snapshotID string) error {
	_, err := r.db.Exec(`DELETE FROM snapshots WHERE id = ?`, snapshotID)
	if err != nil {
		return fmt.Errorf("delete snapshot: %w", err)
	}
	return nil
}

func (r *SnapshotRepository) GetByID(snapshotID string) (*model.Snapshot, error) {
	var snapshot model.Snapshot
	var createdAt string

	err := r.db.QueryRow(`
		SELECT id, user_id, date, COALESCE(note, ''), created_at, created_by
		FROM snapshots
		WHERE id = ?
	`, snapshotID).Scan(
		&snapshot.ID,
		&snapshot.UserID,
		&snapshot.Date,
		&snapshot.Note,
		&createdAt,
		&snapshot.CreatedBy,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("get snapshot by id: %w", err)
	}
	snapshot.CreatedAt = parseSQLiteTime(createdAt)

	items, err := r.GetItems(snapshot.ID)
	if err != nil {
		return nil, err
	}
	events, err := r.GetEvents(snapshot.ID)
	if err != nil {
		return nil, err
	}
	snapshot.Items = items
	snapshot.Events = events

	return &snapshot, nil
}

func (r *SnapshotRepository) ListByUser(userID string) ([]model.Snapshot, error) {
	rows, err := r.db.Query(`
		SELECT id, user_id, date, COALESCE(note, ''), created_at, created_by
		FROM snapshots
		WHERE user_id = ?
		ORDER BY date DESC
	`, userID)
	if err != nil {
		return nil, fmt.Errorf("list snapshots: %w", err)
	}
	defer rows.Close()

	var snapshots []model.Snapshot
	for rows.Next() {
		var snapshot model.Snapshot
		var createdAt string

		if err := rows.Scan(
			&snapshot.ID,
			&snapshot.UserID,
			&snapshot.Date,
			&snapshot.Note,
			&createdAt,
			&snapshot.CreatedBy,
		); err != nil {
			return nil, fmt.Errorf("scan snapshot row: %w", err)
		}

		snapshot.CreatedAt = parseSQLiteTime(createdAt)
		snapshots = append(snapshots, snapshot)
	}

	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate snapshot rows: %w", err)
	}

	return snapshots, nil
}

func (r *SnapshotRepository) GetLatestByUser(userID string) (*model.Snapshot, error) {
	var snapshotID string
	err := r.db.QueryRow(`
		SELECT id FROM snapshots
		WHERE user_id = ?
		ORDER BY date DESC
		LIMIT 1
	`, userID).Scan(&snapshotID)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, fmt.Errorf("get latest snapshot id: %w", err)
	}
	return r.GetByID(snapshotID)
}

func (r *SnapshotRepository) GetItems(snapshotID string) ([]model.SnapshotItem, error) {
	rows, err := r.db.Query(`
		SELECT id, snapshot_id, account_id, balance
		FROM snapshot_items
		WHERE snapshot_id = ?
	`, snapshotID)
	if err != nil {
		return nil, fmt.Errorf("get snapshot items: %w", err)
	}
	defer rows.Close()

	var items []model.SnapshotItem
	for rows.Next() {
		var item model.SnapshotItem
		if err := rows.Scan(&item.ID, &item.SnapshotID, &item.AccountID, &item.Balance); err != nil {
			return nil, fmt.Errorf("scan snapshot item row: %w", err)
		}
		items = append(items, item)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate snapshot item rows: %w", err)
	}
	return items, nil
}

func (r *SnapshotRepository) GetEvents(snapshotID string) ([]model.Event, error) {
	rows, err := r.db.Query(`
		SELECT id, snapshot_id, category, description, amount, created_at
		FROM events
		WHERE snapshot_id = ?
	`, snapshotID)
	if err != nil {
		return nil, fmt.Errorf("get snapshot events: %w", err)
	}
	defer rows.Close()

	var events []model.Event
	for rows.Next() {
		var event model.Event
		var createdAt string

		if err := rows.Scan(
			&event.ID,
			&event.SnapshotID,
			&event.Category,
			&event.Description,
			&event.Amount,
			&createdAt,
		); err != nil {
			return nil, fmt.Errorf("scan event row: %w", err)
		}
		event.CreatedAt = parseSQLiteTime(createdAt)
		events = append(events, event)
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate event rows: %w", err)
	}
	return events, nil
}
