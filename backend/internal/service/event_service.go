package service

import (
	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

type EventService struct {
	eventRepo *repository.EventRepository
}

type EventStatsResult struct {
	ByCategory []repository.EventCategoryStat `json:"by_category"`
	GrandTotal float64                        `json:"grand_total"`
}

func NewEventService(eventRepo *repository.EventRepository) *EventService {
	return &EventService{eventRepo: eventRepo}
}

func (s *EventService) ListBySnapshot(snapshotID string) ([]model.Event, error) {
	return s.eventRepo.ListBySnapshot(snapshotID)
}

func (s *EventService) Update(event *model.Event) error {
	if event.Amount > 0 {
		event.Amount = -event.Amount
	}
	return s.eventRepo.Update(event)
}

func (s *EventService) Delete(eventID string) error {
	return s.eventRepo.Delete(eventID)
}

func (s *EventService) StatsByYear(userID string, year int) (*EventStatsResult, error) {
	stats, grandTotal, err := s.eventRepo.StatsByYear(userID, year)
	if err != nil {
		return nil, err
	}
	return &EventStatsResult{
		ByCategory: stats,
		GrandTotal: grandTotal,
	}, nil
}
