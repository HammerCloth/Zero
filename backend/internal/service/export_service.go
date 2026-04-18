package service

import (
	"bytes"
	"encoding/csv"
	"strconv"

	"zero/backend/internal/repository"
)

type ExportService struct {
	snapshotRepo *repository.SnapshotRepository
}

func NewExportService(snapshotRepo *repository.SnapshotRepository) *ExportService {
	return &ExportService{snapshotRepo: snapshotRepo}
}

func (s *ExportService) ExportSnapshotsCSV(userID string) ([]byte, error) {
	snapshots, err := s.snapshotRepo.ListByUser(userID)
	if err != nil {
		return nil, err
	}

	buf := &bytes.Buffer{}
	writer := csv.NewWriter(buf)

	if err := writer.Write([]string{"snapshot_id", "date", "account_id", "balance"}); err != nil {
		return nil, err
	}

	for _, snap := range snapshots {
		full, err := s.snapshotRepo.GetByID(snap.ID)
		if err != nil {
			return nil, err
		}
		if full == nil {
			continue
		}
		for _, item := range full.Items {
			row := []string{
				full.ID,
				full.Date,
				item.AccountID,
				strconv.FormatFloat(item.Balance, 'f', 2, 64),
			}
			if err := writer.Write(row); err != nil {
				return nil, err
			}
		}
	}

	writer.Flush()
	if err := writer.Error(); err != nil {
		return nil, err
	}
	return buf.Bytes(), nil
}
