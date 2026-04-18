package config

import (
	"os"
	"strconv"
	"time"
)

type Config struct {
	AppEnv         string
	AppPort        string
	DatabasePath   string
	FrontendOrigin string
	AccessSecret   string
	RefreshSecret  string
	AccessTTL      time.Duration
	RefreshTTL     time.Duration
}

func Load() Config {
	return Config{
		AppEnv:         getEnv("APP_ENV", "development"),
		AppPort:        getEnv("APP_PORT", "8080"),
		DatabasePath:   getEnv("DATABASE_PATH", "./data/zero.db"),
		FrontendOrigin: getEnv("FRONTEND_ORIGIN", "http://localhost:5173"),
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
