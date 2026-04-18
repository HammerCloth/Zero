package model

import "time"

type EventCategory string

const (
	EventCategoryRent      EventCategory = "rent"
	EventCategoryTravel    EventCategory = "travel"
	EventCategoryMedical   EventCategory = "medical"
	EventCategoryAppliance EventCategory = "appliance"
	EventCategorySocial    EventCategory = "social"
	EventCategoryOther     EventCategory = "other"
)

type Event struct {
	ID          string        `json:"id" db:"id"`
	SnapshotID  string        `json:"snapshot_id" db:"snapshot_id"`
	Category    EventCategory `json:"category" db:"category"`
	Description string        `json:"description" db:"description"`
	Amount      float64       `json:"amount" db:"amount"`
	CreatedAt   time.Time     `json:"created_at" db:"created_at"`
}
