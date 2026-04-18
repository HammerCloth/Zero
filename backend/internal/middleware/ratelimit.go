package middleware

import (
	"net/http"
	"sync"
	"time"

	"github.com/gin-gonic/gin"
)

type rateRecord struct {
	FailedCount  int
	WindowStart  time.Time
	BlockedUntil time.Time
}

type LoginRateLimiter struct {
	mu          sync.Mutex
	records     map[string]*rateRecord
	maxAttempts int
	window      time.Duration
	blockFor    time.Duration
}

func NewLoginRateLimiter(maxAttempts int, window, blockFor time.Duration) *LoginRateLimiter {
	return &LoginRateLimiter{
		records:     make(map[string]*rateRecord),
		maxAttempts: maxAttempts,
		window:      window,
		blockFor:    blockFor,
	}
}

func (l *LoginRateLimiter) Middleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		key := c.ClientIP()
		if l.isBlocked(key) {
			c.JSON(http.StatusTooManyRequests, gin.H{"error": "登录尝试次数过多，请15分钟后再试"})
			c.Abort()
			return
		}
		c.Next()
	}
}

func (l *LoginRateLimiter) RegisterFailure(key string) {
	l.mu.Lock()
	defer l.mu.Unlock()

	now := time.Now()
	record, ok := l.records[key]
	if !ok {
		l.records[key] = &rateRecord{
			FailedCount: 1,
			WindowStart: now,
		}
		return
	}

	if now.Sub(record.WindowStart) > l.window {
		record.FailedCount = 1
		record.WindowStart = now
		record.BlockedUntil = time.Time{}
		return
	}

	record.FailedCount++
	if record.FailedCount >= l.maxAttempts {
		record.BlockedUntil = now.Add(l.blockFor)
	}
}

func (l *LoginRateLimiter) RegisterSuccess(key string) {
	l.mu.Lock()
	defer l.mu.Unlock()
	delete(l.records, key)
}

func (l *LoginRateLimiter) isBlocked(key string) bool {
	l.mu.Lock()
	defer l.mu.Unlock()

	record, ok := l.records[key]
	if !ok {
		return false
	}

	now := time.Now()
	if !record.BlockedUntil.IsZero() && now.Before(record.BlockedUntil) {
		return true
	}

	if !record.BlockedUntil.IsZero() && now.After(record.BlockedUntil) {
		delete(l.records, key)
		return false
	}

	return false
}
