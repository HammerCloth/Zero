package service

import (
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v4"
)

type TokenService struct {
	accessSecret  []byte
	refreshSecret []byte
	accessTTL     time.Duration
	refreshTTL    time.Duration
}

type Claims struct {
	UserID  string `json:"user_id"`
	IsAdmin bool   `json:"is_admin"`
	Type    string `json:"type"`
	jwt.RegisteredClaims
}

func NewTokenService(accessSecret, refreshSecret string, accessTTL, refreshTTL time.Duration) (*TokenService, error) {
	if accessSecret == "" || refreshSecret == "" {
		return nil, errors.New("token secret is required")
	}
	return &TokenService{
		accessSecret:  []byte(accessSecret),
		refreshSecret: []byte(refreshSecret),
		accessTTL:     accessTTL,
		refreshTTL:    refreshTTL,
	}, nil
}

func (s *TokenService) GenerateAccessToken(userID string, isAdmin bool) (string, error) {
	return s.generateToken(userID, isAdmin, "access", s.accessSecret, s.accessTTL)
}

func (s *TokenService) GenerateRefreshToken(userID string, isAdmin bool) (string, error) {
	return s.generateToken(userID, isAdmin, "refresh", s.refreshSecret, s.refreshTTL)
}

func (s *TokenService) ParseAccessToken(token string) (*Claims, error) {
	return s.parseToken(token, s.accessSecret, "access")
}

func (s *TokenService) ParseRefreshToken(token string) (*Claims, error) {
	return s.parseToken(token, s.refreshSecret, "refresh")
}

func (s *TokenService) generateToken(userID string, isAdmin bool, tokenType string, secret []byte, ttl time.Duration) (string, error) {
	now := time.Now()
	claims := Claims{
		UserID:  userID,
		IsAdmin: isAdmin,
		Type:    tokenType,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   userID,
			ExpiresAt: jwt.NewNumericDate(now.Add(ttl)),
			IssuedAt:  jwt.NewNumericDate(now),
		},
	}

	t := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return t.SignedString(secret)
}

func (s *TokenService) parseToken(token string, secret []byte, expectedType string) (*Claims, error) {
	parsed, err := jwt.ParseWithClaims(token, &Claims{}, func(t *jwt.Token) (interface{}, error) {
		return secret, nil
	})
	if err != nil {
		return nil, err
	}

	claims, ok := parsed.Claims.(*Claims)
	if !ok || !parsed.Valid {
		return nil, errors.New("invalid token")
	}
	if claims.Type != expectedType {
		return nil, errors.New("invalid token type")
	}
	return claims, nil
}
