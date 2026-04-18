package service

import (
	"errors"

	"zero/backend/internal/model"
	"zero/backend/internal/repository"
)

var (
	ErrSetupAlreadyDone   = errors.New("initial setup already completed")
	ErrInvalidCredentials = errors.New("用户名或密码错误")
	ErrInvalidPassword    = errors.New("当前密码错误")
	ErrUserNotFound       = errors.New("用户不存在")
	ErrUserAlreadyExists  = errors.New("用户名已存在")
)

type AuthService struct {
	userRepo     *repository.UserRepository
	accountRepo  *repository.AccountRepository
	tokenService *TokenService
}

type LoginResult struct {
	User         *model.User
	AccessToken  string
	RefreshToken string
}

func NewAuthService(
	userRepo *repository.UserRepository,
	accountRepo *repository.AccountRepository,
	tokenService *TokenService,
) *AuthService {
	return &AuthService{
		userRepo:     userRepo,
		accountRepo:  accountRepo,
		tokenService: tokenService,
	}
}

func (s *AuthService) NeedsSetup() (bool, error) {
	count, err := s.userRepo.CountUsers()
	if err != nil {
		return false, err
	}
	return count == 0, nil
}

func (s *AuthService) SetupAdmin(username, password string) (*LoginResult, error) {
	needsSetup, err := s.NeedsSetup()
	if err != nil {
		return nil, err
	}
	if !needsSetup {
		return nil, ErrSetupAlreadyDone
	}

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
		IsAdmin:            true,
		MustChangePassword: false,
	}

	if err := s.userRepo.Create(user); err != nil {
		return nil, err
	}

	for _, account := range DefaultAccounts(user.ID) {
		accountCopy := account
		if err := s.accountRepo.Create(&accountCopy); err != nil {
			return nil, err
		}
	}

	return s.loginByUser(user)
}

func (s *AuthService) Login(username, password string) (*LoginResult, error) {
	user, err := s.userRepo.GetByUsername(username)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, ErrInvalidCredentials
	}

	if err := ComparePassword(user.PasswordHash, password); err != nil {
		return nil, ErrInvalidCredentials
	}

	return s.loginByUser(user)
}

func (s *AuthService) Refresh(refreshToken string) (*LoginResult, error) {
	claims, err := s.tokenService.ParseRefreshToken(refreshToken)
	if err != nil {
		return nil, err
	}

	user, err := s.userRepo.GetByID(claims.UserID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, ErrUserNotFound
	}

	return s.loginByUser(user)
}

func (s *AuthService) Me(userID string) (*model.User, error) {
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return nil, err
	}
	if user == nil {
		return nil, ErrUserNotFound
	}
	user.PasswordHash = ""
	return user, nil
}

func (s *AuthService) ChangePassword(userID, currentPassword, newPassword string) error {
	user, err := s.userRepo.GetByID(userID)
	if err != nil {
		return err
	}
	if user == nil {
		return ErrUserNotFound
	}

	if err := ComparePassword(user.PasswordHash, currentPassword); err != nil {
		return ErrInvalidPassword
	}

	newHash, err := HashPassword(newPassword)
	if err != nil {
		return err
	}

	return s.userRepo.UpdatePassword(userID, newHash, false)
}

func (s *AuthService) loginByUser(user *model.User) (*LoginResult, error) {
	accessToken, err := s.tokenService.GenerateAccessToken(user.ID, user.IsAdmin)
	if err != nil {
		return nil, err
	}
	refreshToken, err := s.tokenService.GenerateRefreshToken(user.ID, user.IsAdmin)
	if err != nil {
		return nil, err
	}

	return &LoginResult{
		User:         user,
		AccessToken:  accessToken,
		RefreshToken: refreshToken,
	}, nil
}
