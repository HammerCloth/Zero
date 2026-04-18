package model

import "time"

type AccountType string

const (
	AccountTypeCash        AccountType = "cash"
	AccountTypeDeposit     AccountType = "deposit"
	AccountTypeFund        AccountType = "fund"
	AccountTypePension     AccountType = "pension"
	AccountTypeHousingFund AccountType = "housing_fund"
	AccountTypeCredit      AccountType = "credit"
)

type AccountOwner string

const (
	AccountOwnerA      AccountOwner = "A"
	AccountOwnerB      AccountOwner = "B"
	AccountOwnerShared AccountOwner = "shared"
)

type Account struct {
	ID        string       `json:"id" db:"id"`
	UserID    string       `json:"user_id" db:"user_id"`
	Name      string       `json:"name" db:"name"`
	Type      AccountType  `json:"type" db:"type"`
	Owner     AccountOwner `json:"owner" db:"owner"`
	SortOrder int          `json:"sort_order" db:"sort_order"`
	IsActive  bool         `json:"is_active" db:"is_active"`
	CreatedAt time.Time    `json:"created_at" db:"created_at"`
}
