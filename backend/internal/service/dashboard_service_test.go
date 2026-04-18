package service

import (
	"math"
	"testing"
	"time"
)

func TestRangeStart(t *testing.T) {
	loc := time.Local
	now := time.Date(2024, 6, 15, 12, 0, 0, 0, loc)

	t.Run("default_empty", func(t *testing.T) {
		got := rangeStart(now, "all")
		if !got.IsZero() {
			t.Fatalf("got %v", got)
		}
	})

	t.Run("3m", func(t *testing.T) {
		got := rangeStart(now, "3m")
		want := now.AddDate(0, -3, 0)
		if !got.Equal(want) {
			t.Fatalf("got %v want %v", got, want)
		}
	})

	t.Run("1y", func(t *testing.T) {
		got := rangeStart(now, "1y")
		want := now.AddDate(-1, 0, 0)
		if !got.Equal(want) {
			t.Fatalf("got %v want %v", got, want)
		}
	})
}

func TestAnnualizedReturnPercent(t *testing.T) {
	loc := time.Local
	start := time.Date(2020, 1, 1, 0, 0, 0, 0, loc)
	end := time.Date(2022, 1, 1, 0, 0, 0, 0, loc)

	t.Run("zero_initial", func(t *testing.T) {
		if annualizedReturnPercent(0, 100, start, end) != 0 {
			t.Fatal("expected 0")
		}
	})

	t.Run("negative_years", func(t *testing.T) {
		if annualizedReturnPercent(100, 200, end, start) != 0 {
			t.Fatal("expected 0")
		}
	})

	t.Run("double_over_two_calendar_years", func(t *testing.T) {
		// 100 -> 400; years come from the same duration formula as production (not necessarily 2.0).
		got := annualizedReturnPercent(100, 400, start, end)
		years := end.Sub(start).Hours() / 24 / 365
		want := (math.Pow(4, 1/years) - 1) * 100
		if math.Abs(got-want) > 1e-9 {
			t.Fatalf("got %v want %v (years=%v)", got, want, years)
		}
	})
}
