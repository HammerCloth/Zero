package repository

import (
	"database/sql"
	"fmt"

	"zero/backend/internal/model"
)

type EventRepository struct {
	db *sql.DB
}

func NewEventRepository(db *sql.DB) *EventRepository {
	return &EventRepository{db: db}
}

type EventCategoryStat struct {
	Category model.EventCategory `json:"category"`
	Total    float64             `json:"total"`
	Count    int                 `json:"count"`
}

func (r *EventRepository) Create(event *model.Event) error {
	_, err := r.db.Exec(`
		INSERT INTO events(id, snapshot_id, category, description, amount)
		VALUES(?, ?, ?, ?, ?)
	`, event.ID, event.SnapshotID, event.Category, event.Description, event.Amount)
	if err != nil {
		return fmt.Errorf("create event: %w", err)
	}
	return nil
}

func (r *EventRepository) Update(event *model.Event) error {
	_, err := r.db.Exec(`
		UPDATE events
		SET category = ?, description = ?, amount = ?
		WHERE id = ?
	`, event.Category, event.Description, event.Amount, event.ID)
	if err != nil {
		return fmt.Errorf("update event: %w", err)
	}
	return nil
}

func (r *EventRepository) Delete(eventID string) error {
	_, err := r.db.Exec(`DELETE FROM events WHERE id = ?`, eventID)
	if err != nil {
		return fmt.Errorf("delete event: %w", err)
	}
	return nil
}

func (r *EventRepository) ListBySnapshot(snapshotID string) ([]model.Event, error) {
	rows, err := r.db.Query(`
		SELECT id, snapshot_id, category, description, amount, created_at
		FROM events
		WHERE snapshot_id = ?
		ORDER BY created_at ASC
	`, snapshotID)
	if err != nil {
		return nil, fmt.Errorf("list events by snapshot: %w", err)
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

func (r *EventRepository) StatsByYear(userID string, year int) ([]EventCategoryStat, float64, error) {
	rows, err := r.db.Query(`
		SELECT e.category, ABS(SUM(e.amount)) AS total, COUNT(1) AS count
		FROM events e
		JOIN snapshots s ON s.id = e.snapshot_id
		WHERE s.user_id = ? AND strftime('%Y', s.date) = ?
		GROUP BY e.category
		ORDER BY total DESC
	`, userID, fmt.Sprintf("%04d", year))
	if err != nil {
		return nil, 0, fmt.Errorf("query event stats by year: %w", err)
	}
	defer rows.Close()

	var stats []EventCategoryStat
	var grandTotal float64

	for rows.Next() {
		var stat EventCategoryStat
		if err := rows.Scan(&stat.Category, &stat.Total, &stat.Count); err != nil {
			return nil, 0, fmt.Errorf("scan event stat row: %w", err)
		}
		grandTotal += stat.Total
		stats = append(stats, stat)
	}

	if err := rows.Err(); err != nil {
		return nil, 0, fmt.Errorf("iterate event stat rows: %w", err)
	}
	return stats, grandTotal, nil
}
