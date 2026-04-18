package service

import (
	"os"
	"path/filepath"
	"runtime"
	"testing"

	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

func testBackendRoot(t *testing.T) string {
	t.Helper()
	_, f, _, ok := runtime.Caller(1)
	if !ok {
		t.Fatal("runtime.Caller failed")
	}
	return filepath.Clean(filepath.Join(filepath.Dir(f), "../.."))
}

func withBackendWd(t *testing.T, fn func()) {
	t.Helper()
	root := testBackendRoot(t)
	wd, err := os.Getwd()
	if err != nil {
		t.Fatal(err)
	}
	if err := os.Chdir(root); err != nil {
		t.Fatal(err)
	}
	defer func() { _ = os.Chdir(wd) }()
	fn()
}

func TestCalculateNetWorth(t *testing.T) {
	tests := []struct {
		name  string
		items []model.SnapshotItem
		want  float64
	}{
		{"empty", nil, 0},
		{"empty_slice", []model.SnapshotItem{}, 0},
		{"single", []model.SnapshotItem{{Balance: 100}}, 100},
		{"mixed", []model.SnapshotItem{{Balance: 100}, {Balance: -30}, {Balance: 0.5}}, 70.5},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if got := CalculateNetWorth(tt.items); got != tt.want {
				t.Fatalf("got %v want %v", got, tt.want)
			}
		})
	}
}

func TestNormalizeSnapshotItems_sortAndIDs(t *testing.T) {
	assigner := snapshotIDAssigner{snapshotID: "snap-1"}
	items := []model.SnapshotItem{
		{AccountID: "z", Balance: 1},
		{AccountID: "a", Balance: 2, ID: "keep"},
	}
	out := normalizeSnapshotItems(assigner, items)
	if len(out) != 2 {
		t.Fatalf("len %d", len(out))
	}
	if out[0].AccountID != "a" || out[1].AccountID != "z" {
		t.Fatalf("sort order: %+v", out)
	}
	if out[0].ID != "keep" || out[0].SnapshotID != "snap-1" {
		t.Fatalf("first: %+v", out[0])
	}
	if out[1].ID == "" || out[1].SnapshotID != "snap-1" {
		t.Fatalf("second: %+v", out[1])
	}
}

func TestNormalizeEvents_amountAndSnapshotID(t *testing.T) {
	assigner := snapshotIDAssigner{snapshotID: "s1"}
	out := normalizeEvents(assigner, []model.Event{
		{Amount: 100, Description: "x"},
		{Amount: -50, Description: "y"},
	})
	if len(out) != 2 {
		t.Fatal(len(out))
	}
	if out[0].Amount != -100 {
		t.Fatalf("positive expense -> negative: %v", out[0].Amount)
	}
	if out[1].Amount != -50 {
		t.Fatalf("already negative unchanged: %v", out[1].Amount)
	}
	for _, e := range out {
		if e.SnapshotID != "s1" || e.ID == "" {
			t.Fatalf("event: %+v", e)
		}
	}
}

func TestValidateAllAccountBalances_missingAccount(t *testing.T) {
	withBackendWd(t, func() {
		dbPath := filepath.Join(t.TempDir(), "test.db")
		db, err := repository.InitDB(dbPath)
		if err != nil {
			t.Fatal(err)
		}
		defer db.Close()

		_, err = db.Exec(`
			INSERT INTO users (id, username, password_hash, is_admin, must_change_password)
			VALUES ('u1', 'u1', 'h', 1, 0)
		`)
		if err != nil {
			t.Fatal(err)
		}
		_, err = db.Exec(`
			INSERT INTO accounts (id, user_id, name, type, owner, sort_order, is_active)
			VALUES ('acc1', 'u1', 'A', 'cash', 'A', 0, 1),
			       ('acc2', 'u1', 'B', 'cash', 'A', 1, 1)
		`)
		if err != nil {
			t.Fatal(err)
		}

		accRepo := repository.NewAccountRepository(db)
		snapRepo := repository.NewSnapshotRepository(db)
		svc := NewSnapshotService(snapRepo, accRepo)

		err = svc.validateAllAccountBalances("u1", []model.SnapshotItem{{AccountID: "acc1", Balance: 1}})
		if err != ErrMissingBalances {
			t.Fatalf("want ErrMissingBalances got %v", err)
		}

		err = svc.validateAllAccountBalances("u1", []model.SnapshotItem{
			{AccountID: "acc2", Balance: 1},
			{AccountID: "acc1", Balance: 2},
		})
		if err != nil {
			t.Fatal(err)
		}
	})
}
