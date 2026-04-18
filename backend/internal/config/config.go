package config

import (
	"os"
	"strconv"
	"strings"
	"time"
)

type Config struct {
	AppEnv          string
	AppPort         string
	DatabasePath    string
	FrontendOrigins []string // 来自 FRONTEND_ORIGIN，支持英文逗号分隔多个来源（如 www 与根域）
	AccessSecret   string
	RefreshSecret  string
	AccessTTL      time.Duration
	RefreshTTL     time.Duration
}

func Load() Config {
	return Config{
		AppEnv:          getEnv("APP_ENV", "development"),
		AppPort:         getEnv("APP_PORT", "8080"),
		DatabasePath:    getEnv("DATABASE_PATH", "./data/zero.db"),
		FrontendOrigins: splitOrigins(getEnv("FRONTEND_ORIGIN", "http://localhost:5173")),
		AccessSecret:   getEnv("JWT_ACCESS_SECRET", "dev-access-secret"),
		RefreshSecret:  getEnv("JWT_REFRESH_SECRET", "dev-refresh-secret"),
		AccessTTL:      getDurationEnv("JWT_ACCESS_TTL_SECONDS", 3600*time.Second),
		RefreshTTL:     getDurationEnv("JWT_REFRESH_TTL_SECONDS", 30*24*time.Hour),
	}
}

func getEnv(key, fallback string) string {
	value := os.Getenv(key)
	if value == "" {
		return fallback
	}
	return value
}

func splitOrigins(raw string) []string {
	parts := strings.Split(raw, ",")
	out := make([]string, 0, len(parts))
	for _, p := range parts {
		p = strings.TrimSpace(p)
		if p != "" {
			out = append(out, p)
		}
	}
	if len(out) == 0 {
		return []string{"http://localhost:5173"}
	}
	return out
}

func getDurationEnv(key string, fallback time.Duration) time.Duration {
	raw := os.Getenv(key)
	if raw == "" {
		return fallback
	}

	seconds, err := strconv.Atoi(raw)
	if err != nil || seconds <= 0 {
		return fallback
	}
	return time.Duration(seconds) * time.Second
}
