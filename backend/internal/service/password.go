package service

import (
	"errors"

	"golang.org/x/crypto/bcrypt"
)

const MinPasswordLength = 8

func ValidatePassword(password string) error {
	if len(password) < MinPasswordLength {
		return errors.New("密码至少需要8个字符")
	}
	return nil
}

func HashPassword(password string) (string, error) {
	if err := ValidatePassword(password); err != nil {
		return "", err
	}

	hash, err := bcrypt.GenerateFromPassword([]byte(password), 12)
	if err != nil {
		return "", err
	}
	return string(hash), nil
}

func ComparePassword(hash, password string) error {
	return bcrypt.CompareHashAndPassword([]byte(hash), []byte(password))
}
