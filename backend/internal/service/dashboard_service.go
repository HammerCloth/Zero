package service

import (
	"math"
	"time"

	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

type DashboardService struct {
	snapshotRepo *repository.SnapshotRepository
	accountRepo  *repository.AccountRepository
	// now is injectable for tests; production uses time.Now via constructor.
	now func() time.Time
}

type Summary struct {
	NetWorth         float64 `json:"net_worth"`
	MonthlyChange    float64 `json:"monthly_change"`
	AnnualChange     float64 `json:"annual_change"`
	AnnualizedReturn float64 `json:"annualized_return"`
}

type TrendPoint struct {
	Date     string  `json:"date"`
	NetWorth float64 `json:"net_worth"`
}

type MonthlyGrowthPoint struct {
	Month  string  `json:"month"`
	Change float64 `json:"change"`
}

type Composition struct {
	ByType  map[string]float64 `json:"by_type"`
	ByOwner map[string]float64 `json:"by_owner"`
}

func NewDashboardService(
	snapshotRepo *repository.SnapshotRepository,
	accountRepo *repository.AccountRepository,
) *DashboardService {
	return &DashboardService{
		snapshotRepo: snapshotRepo,
		accountRepo:  accountRepo,
		now:          time.Now,
	}
}

func (s *DashboardService) Summary(userID string) (*Summary, error) {
	snapshots, err := s.snapshotRepo.ListByUser(userID)
	if err != nil {
		return nil, err
	}
	if len(snapshots) == 0 {
		return &Summary{}, nil
	}

	latest, err := s.snapshotRepo.GetByID(snapshots[0].ID)
	if err != nil {
		return nil, err
	}
	if latest == nil {
		return &Summary{}, nil
	}

	currentNetWorth := CalculateNetWorth(latest.Items)
	monthlyChange := 0.0
	if len(snapshots) > 1 {
		prev, err := s.snapshotRepo.GetByID(snapshots[1].ID)
		if err != nil {
			return nil, err
		}
		if prev != nil {
			monthlyChange = currentNetWorth - CalculateNetWorth(prev.Items)
		}
	}

	annualChange := 0.0
	nowFn := s.now
	if nowFn == nil {
		nowFn = time.Now
	}
	startOfYear := time.Date(nowFn().Year(), 1, 1, 0, 0, 0, 0, time.Local)
	var yearBase float64
	var yearBaseFound bool
	for i := len(snapshots) - 1; i >= 0; i-- {
		t, err := time.Parse("2006-01-02", snapshots[i].Date)
		if err != nil {
			continue
		}
		if t.Before(startOfYear) || t.Equal(startOfYear) {
			baseSnapshot, err := s.snapshotRepo.GetByID(snapshots[i].ID)
			if err != nil {
				return nil, err
			}
			if baseSnapshot != nil {
				yearBase = CalculateNetWorth(baseSnapshot.Items)
				yearBaseFound = true
				break
			}
		}
	}
	if yearBaseFound {
		annualChange = currentNetWorth - yearBase
	}

	annualizedReturn := CalculateAnnualizedReturn(snapshots, s.snapshotRepo)

	return &Summary{
		NetWorth:         currentNetWorth,
		MonthlyChange:    monthlyChange,
		AnnualChange:     annualChange,
		AnnualizedReturn: annualizedReturn,
	}, nil
}

func (s *DashboardService) Trend(userID, rangeKey string) ([]TrendPoint, error) {
	snapshots, err := s.snapshotRepo.ListByUser(userID)
	if err != nil {
		return nil, err
	}
	nowFn := s.now
	if nowFn == nil {
		nowFn = time.Now
	}
	startDate := rangeStart(nowFn(), rangeKey)

	points := make([]TrendPoint, 0, len(snapshots))
	for i := len(snapshots) - 1; i >= 0; i-- {
		snapshot := snapshots[i]
		if !startDate.IsZero() {
			t, err := time.Parse("2006-01-02", snapshot.Date)
			if err == nil && t.Before(startDate) {
				continue
			}
		}
		full, err := s.snapshotRepo.GetByID(snapshot.ID)
		if err != nil {
			return nil, err
		}
		if full == nil {
			continue
		}
		points = append(points, TrendPoint{
			Date:     snapshot.Date,
			NetWorth: CalculateNetWorth(full.Items),
		})
	}
	return points, nil
}

func (s *DashboardService) Composition(userID string) (*Composition, error) {
	latest, err := s.snapshotRepo.GetLatestByUser(userID)
	if err != nil {
		return nil, err
	}
	if latest == nil {
		return &Composition{
			ByType:  map[string]float64{},
			ByOwner: map[string]float64{},
		}, nil
	}

	accounts, err := s.accountRepo.ListAll(userID)
	if err != nil {
		return nil, err
	}
	accountMap := make(map[string]model.Account, len(accounts))
	for _, account := range accounts {
		accountMap[account.ID] = account
	}

	byType := make(map[string]float64)
	byOwner := make(map[string]float64)
	for _, item := range latest.Items {
		account, ok := accountMap[item.AccountID]
		if !ok {
			continue
		}
		byType[string(account.Type)] += item.Balance
		byOwner[string(account.Owner)] += item.Balance
	}

	return &Composition{
		ByType:  byType,
		ByOwner: byOwner,
	}, nil
}

func (s *DashboardService) MonthlyGrowth(userID string, year int) ([]MonthlyGrowthPoint, error) {
	snapshots, err := s.snapshotRepo.ListByUser(userID)
	if err != nil {
		return nil, err
	}

	points := make([]MonthlyGrowthPoint, 0, len(snapshots))
	var prevNetWorth *float64

	for i := len(snapshots) - 1; i >= 0; i-- {
		snapshot := snapshots[i]
		t, err := time.Parse("2006-01-02", snapshot.Date)
		if err != nil || t.Year() != year {
			continue
		}

		full, err := s.snapshotRepo.GetByID(snapshot.ID)
		if err != nil {
			return nil, err
		}
		if full == nil {
			continue
		}

		netWorth := CalculateNetWorth(full.Items)
		change := 0.0
		if prevNetWorth != nil {
			change = netWorth - *prevNetWorth
		}
		prevNetWorth = &netWorth

		points = append(points, MonthlyGrowthPoint{
			Month:  t.Format("2006-01"),
			Change: change,
		})
	}

	return points, nil
}

func CalculateAnnualizedReturn(snapshots []model.Snapshot, snapshotRepo *repository.SnapshotRepository) float64 {
	if len(snapshots) < 2 {
		return 0
	}

	oldest, err := snapshotRepo.GetByID(snapshots[len(snapshots)-1].ID)
	if err != nil || oldest == nil {
		return 0
	}
	newest, err := snapshotRepo.GetByID(snapshots[0].ID)
	if err != nil || newest == nil {
		return 0
	}

	initial := CalculateNetWorth(oldest.Items)
	current := CalculateNetWorth(newest.Items)

	start, err := time.Parse("2006-01-02", oldest.Date)
	if err != nil {
		return 0
	}
	end, err := time.Parse("2006-01-02", newest.Date)
	if err != nil {
		return 0
	}

	return annualizedReturnPercent(initial, current, start, end)
}

// annualizedReturnPercent is the CAGR-style percentage over [start, end].
func annualizedReturnPercent(initial, current float64, start, end time.Time) float64 {
	if initial <= 0 {
		return 0
	}
	years := end.Sub(start).Hours() / 24 / 365
	if years <= 0 {
		return 0
	}
	return (math.Pow(current/initial, 1/years) - 1) * 100
}

func rangeStart(now time.Time, rangeKey string) time.Time {
	switch rangeKey {
	case "3m":
		return now.AddDate(0, -3, 0)
	case "6m":
		return now.AddDate(0, -6, 0)
	case "1y":
		return now.AddDate(-1, 0, 0)
	case "2y":
		return now.AddDate(-2, 0, 0)
	default:
		return time.Time{}
	}
}
