package service

import (
	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

type UserService struct {
	userRepo *repository.UserRepository
}

func NewUserService(userRepo *repository.UserRepository) *UserService {
	return &UserService{userRepo: userRepo}
}

func (s *UserService) ListUsers() ([]model.User, error) {
	return s.userRepo.List()
}

func (s *UserService) CreateUser(username, password string, isAdmin bool) (*model.User, error) {
	existing, err := s.userRepo.GetByUsername(username)
	if err != nil {
		return nil, err
	}
	if existing != nil {
		return nil, ErrUserAlreadyExists
	}

	hash, err := HashPassword(password)
	if err != nil {
		return nil, err
	}

	user := &model.User{
		ID:                 newID(),
		Username:           username,
		PasswordHash:       hash,
		IsAdmin:            isAdmin,
		MustChangePassword: true,
	}

	if err := s.userRepo.Create(user); err != nil {
		return nil, err
	}
	return user, nil
}

func (s *UserService) ResetPassword(userID, newPassword string) error {
	hash, err := HashPassword(newPassword)
	if err != nil {
		return err
	}
	return s.userRepo.UpdatePassword(userID, hash, true)
}
