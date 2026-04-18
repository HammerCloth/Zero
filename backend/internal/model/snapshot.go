package model

import (
	"time"
)

type Snapshot struct {
	ID        string         `json:"id" db:"id"`
	UserID    string         `json:"user_id" db:"user_id"`
	Date      string         `json:"date" db:"date"`
	Note      string         `json:"note,omitempty" db:"note"`
	CreatedAt time.Time      `json:"created_at" db:"created_at"`
	CreatedBy string         `json:"created_by" db:"created_by"`
	Items     []SnapshotItem `json:"items,omitempty"`
	Events    []Event        `json:"events,omitempty"`
}

type SnapshotItem struct {
	ID         string  `json:"id" db:"id"`
	SnapshotID string  `json:"snapshot_id" db:"snapshot_id"`
	AccountID  string  `json:"account_id" db:"account_id"`
	Balance    float64 `json:"balance" db:"balance"`
}
