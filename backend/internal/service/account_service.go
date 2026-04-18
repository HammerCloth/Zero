package service

import (
	"errors"

	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

var ErrAccountNotFound = errors.New("账户不存在")

type AccountService struct {
	accountRepo *repository.AccountRepository
}

func NewAccountService(accountRepo *repository.AccountRepository) *AccountService {
	return &AccountService{accountRepo: accountRepo}
}

func (s *AccountService) List(userID string) ([]model.Account, error) {
	return s.accountRepo.ListActive(userID)
}

func (s *AccountService) Create(userID, name string, accountType model.AccountType, owner model.AccountOwner) (*model.Account, error) {
	accounts, err := s.accountRepo.ListAll(userID)
	if err != nil {
		return nil, err
	}

	account := &model.Account{
		ID:        newID(),
		UserID:    userID,
		Name:      name,
		Type:      accountType,
		Owner:     owner,
		SortOrder: len(accounts) + 1,
		IsActive:  true,
	}

	if err := s.accountRepo.Create(account); err != nil {
		return nil, err
	}
	return account, nil
}

func (s *AccountService) Update(accountID, name string, accountType model.AccountType, owner model.AccountOwner, sortOrder int) error {
	existing, err := s.accountRepo.GetByID(accountID)
	if err != nil {
		return err
	}
	if existing == nil {
		return ErrAccountNotFound
	}

	existing.Name = name
	existing.Type = accountType
	existing.Owner = owner
	existing.SortOrder = sortOrder
	return s.accountRepo.Update(existing)
}

func (s *AccountService) Deactivate(accountID string) error {
	existing, err := s.accountRepo.GetByID(accountID)
	if err != nil {
		return err
	}
	if existing == nil {
		return ErrAccountNotFound
	}
	return s.accountRepo.Deactivate(accountID)
}

func (s *AccountService) Reorder(userID string, accountIDs []string) error {
	return s.accountRepo.Reorder(userID, accountIDs)
}

func DefaultAccounts(userID string) []model.Account {
	return []model.Account{
		{ID: newID(), UserID: userID, Name: "A工资卡", Type: model.AccountTypeCash, Owner: model.AccountOwnerA, SortOrder: 1, IsActive: true},
		{ID: newID(), UserID: userID, Name: "B工资卡", Type: model.AccountTypeCash, Owner: model.AccountOwnerB, SortOrder: 2, IsActive: true},
		{ID: newID(), UserID: userID, Name: "小荷包", Type: model.AccountTypeCash, Owner: model.AccountOwnerShared, SortOrder: 3, IsActive: true},
		{ID: newID(), UserID: userID, Name: "招行存单", Type: model.AccountTypeDeposit, Owner: model.AccountOwnerShared, SortOrder: 4, IsActive: true},
		{ID: newID(), UserID: userID, Name: "月月宝", Type: model.AccountTypeDeposit, Owner: model.AccountOwnerShared, SortOrder: 5, IsActive: true},
		{ID: newID(), UserID: userID, Name: "国内标普QDII", Type: model.AccountTypeFund, Owner: model.AccountOwnerA, SortOrder: 6, IsActive: true},
		{ID: newID(), UserID: userID, Name: "海外基金", Type: model.AccountTypeFund, Owner: model.AccountOwnerB, SortOrder: 7, IsActive: true},
		{ID: newID(), UserID: userID, Name: "个人养老金", Type: model.AccountTypePension, Owner: model.AccountOwnerA, SortOrder: 8, IsActive: true},
		{ID: newID(), UserID: userID, Name: "A住房公积金", Type: model.AccountTypeHousingFund, Owner: model.AccountOwnerA, SortOrder: 9, IsActive: true},
		{ID: newID(), UserID: userID, Name: "B住房公积金", Type: model.AccountTypeHousingFund, Owner: model.AccountOwnerB, SortOrder: 10, IsActive: true},
		{ID: newID(), UserID: userID, Name: "信用卡", Type: model.AccountTypeCredit, Owner: model.AccountOwnerA, SortOrder: 11, IsActive: true},
	}
}

func (s *AccountService) InitializeDefaultAccounts(userID string) error {
	for _, account := range DefaultAccounts(userID) {
		accountCopy := account
		if err := s.accountRepo.Create(&accountCopy); err != nil {
			return err
		}
	}
	return nil
}
