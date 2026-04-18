package service

import (
	"errors"
	"sort"

	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

var (
	ErrSnapshotNotFound = errors.New("快照不存在")
	ErrMissingBalances  = errors.New("存在未填写余额的账户")
)

type SnapshotService struct {
	snapshotRepo *repository.SnapshotRepository
	accountRepo  *repository.AccountRepository
}

func NewSnapshotService(snapshotRepo *repository.SnapshotRepository, accountRepo *repository.AccountRepository) *SnapshotService {
	return &SnapshotService{
		snapshotRepo: snapshotRepo,
		accountRepo:  accountRepo,
	}
}

func (s *SnapshotService) List(userID string) ([]model.Snapshot, error) {
	snapshots, err := s.snapshotRepo.ListByUser(userID)
	if err != nil {
		return nil, err
	}
	return snapshots, nil
}

func (s *SnapshotService) Get(snapshotID string) (*model.Snapshot, error) {
	snapshot, err := s.snapshotRepo.GetByID(snapshotID)
	if err != nil {
		return nil, err
	}
	if snapshot == nil {
		return nil, ErrSnapshotNotFound
	}
	return snapshot, nil
}

func (s *SnapshotService) GetLatest(userID string) (*model.Snapshot, error) {
	return s.snapshotRepo.GetLatestByUser(userID)
}

func (s *SnapshotService) Create(userID, createdBy, date, note string, items []model.SnapshotItem, events []model.Event) (*model.Snapshot, error) {
	if err := s.validateAllAccountBalances(userID, items); err != nil {
		return nil, err
	}

	snapshot := &model.Snapshot{
		ID:        newID(),
		UserID:    userID,
		Date:      date,
		Note:      note,
		CreatedBy: createdBy,
		Items:     normalizeSnapshotItems(snapshotIDAssigner{}, items),
		Events:    normalizeEvents(snapshotIDAssigner{}, events),
	}

	for idx := range snapshot.Items {
		snapshot.Items[idx].SnapshotID = snapshot.ID
	}
	for idx := range snapshot.Events {
		snapshot.Events[idx].SnapshotID = snapshot.ID
	}

	if err := s.snapshotRepo.Create(snapshot); err != nil {
		return nil, err
	}
	return s.snapshotRepo.GetByID(snapshot.ID)
}

func (s *SnapshotService) Update(snapshotID, userID, date, note string, items []model.SnapshotItem, events []model.Event) (*model.Snapshot, error) {
	existing, err := s.snapshotRepo.GetByID(snapshotID)
	if err != nil {
		return nil, err
	}
	if existing == nil {
		return nil, ErrSnapshotNotFound
	}
	if err := s.validateAllAccountBalances(userID, items); err != nil {
		return nil, err
	}

	existing.Date = date
	existing.Note = note
	existing.Items = normalizeSnapshotItems(snapshotIDAssigner{snapshotID: snapshotID}, items)
	existing.Events = normalizeEvents(snapshotIDAssigner{snapshotID: snapshotID}, events)

	if err := s.snapshotRepo.Update(existing); err != nil {
		return nil, err
	}
	return s.snapshotRepo.GetByID(snapshotID)
}

func (s *SnapshotService) Delete(snapshotID string) error {
	return s.snapshotRepo.Delete(snapshotID)
}

func CalculateNetWorth(items []model.SnapshotItem) float64 {
	var total float64
	for _, item := range items {
		total += item.Balance
	}
	return total
}

func (s *SnapshotService) validateAllAccountBalances(userID string, items []model.SnapshotItem) error {
	accounts, err := s.accountRepo.ListActive(userID)
	if err != nil {
		return err
	}

	itemMap := make(map[string]struct{}, len(items))
	for _, item := range items {
		itemMap[item.AccountID] = struct{}{}
	}

	for _, account := range accounts {
		if _, ok := itemMap[account.ID]; !ok {
			return ErrMissingBalances
		}
	}
	return nil
}

type snapshotIDAssigner struct {
	snapshotID string
}

func normalizeSnapshotItems(assigner snapshotIDAssigner, items []model.SnapshotItem) []model.SnapshotItem {
	normalized := make([]model.SnapshotItem, 0, len(items))
	for _, item := range items {
		if item.ID == "" {
			item.ID = newID()
		}
		if item.SnapshotID == "" {
			item.SnapshotID = assigner.snapshotID
		}
		normalized = append(normalized, item)
	}
	sort.Slice(normalized, func(i, j int) bool {
		return normalized[i].AccountID < normalized[j].AccountID
	})
	return normalized
}

func normalizeEvents(assigner snapshotIDAssigner, events []model.Event) []model.Event {
	normalized := make([]model.Event, 0, len(events))
	for _, event := range events {
		if event.ID == "" {
			event.ID = newID()
		}
		if event.SnapshotID == "" {
			event.SnapshotID = assigner.snapshotID
		}
		// Normalize expense amount to negative.
		if event.Amount > 0 {
			event.Amount = -event.Amount
		}
		normalized = append(normalized, event)
	}
	return normalized
}
