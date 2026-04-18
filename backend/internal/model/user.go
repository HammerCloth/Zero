package model

import "time"

type User struct {
	ID                 string    `json:"id" db:"id"`
	Username           string    `json:"username" db:"username"`
	PasswordHash       string    `json:"-" db:"password_hash"`
	IsAdmin            bool      `json:"is_admin" db:"is_admin"`
	MustChangePassword bool      `json:"must_change_password" db:"must_change_password"`
	CreatedAt          time.Time `json:"created_at" db:"created_at"`
}
