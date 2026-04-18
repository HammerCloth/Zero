package handler

import (
	"errors"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/service"
)

const refreshCookieName = "refresh_token"

type AuthHandler struct {
	authService *service.AuthService
	limiter     *middleware.LoginRateLimiter
}

func NewAuthHandler(authService *service.AuthService, limiter *middleware.LoginRateLimiter) *AuthHandler {
	return &AuthHandler{
		authService: authService,
		limiter:     limiter,
	}
}

type setupRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
}

type loginRequest struct {
	Username string `json:"username" binding:"required"`
	Password string `json:"password" binding:"required"`
	Remember bool   `json:"remember_me"`
}

type changePasswordRequest struct {
	CurrentPassword string `json:"current_password" binding:"required"`
	NewPassword     string `json:"new_password" binding:"required"`
}

func (h *AuthHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("/status", h.Status)
	rg.POST("/setup", h.Setup)
	rg.POST("/login", h.Login)
	rg.POST("/refresh", h.Refresh)
	rg.POST("/logout", h.Logout)
}

func (h *AuthHandler) RegisterProtectedRoutes(rg *gin.RouterGroup) {
	rg.GET("/me", h.Me)
	rg.PUT("/password", h.ChangePassword)
}

func (h *AuthHandler) Status(c *gin.Context) {
	needsSetup, err := h.authService.NeedsSetup()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"needs_setup": needsSetup})
}

func (h *AuthHandler) Setup(c *gin.Context) {
	var req setupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	result, err := h.authService.SetupAdmin(req.Username, req.Password)
	if err != nil {
		switch {
		case errors.Is(err, service.ErrSetupAlreadyDone):
			c.JSON(http.StatusConflict, gin.H{"error": "初始化已完成"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}

	setRefreshCookie(c, result.RefreshToken, true)
	c.JSON(http.StatusOK, gin.H{
		"user":         result.User,
		"access_token": result.AccessToken,
	})
}

func (h *AuthHandler) Login(c *gin.Context) {
	var req loginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	ip := c.ClientIP()
	result, err := h.authService.Login(req.Username, req.Password)
	if err != nil {
		h.limiter.RegisterFailure(ip)
		if errors.Is(err, service.ErrInvalidCredentials) {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "用户名或密码错误"})
			return
		}
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	h.limiter.RegisterSuccess(ip)

	setRefreshCookie(c, result.RefreshToken, req.Remember)
	c.JSON(http.StatusOK, gin.H{
		"user":         result.User,
		"access_token": result.AccessToken,
	})
}

func (h *AuthHandler) Refresh(c *gin.Context) {
	token, err := c.Cookie(refreshCookieName)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
		return
	}

	result, err := h.authService.Refresh(token)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
		return
	}

	setRefreshCookie(c, result.RefreshToken, true)
	c.JSON(http.StatusOK, gin.H{
		"user":         result.User,
		"access_token": result.AccessToken,
	})
}

func (h *AuthHandler) Logout(c *gin.Context) {
	c.SetCookie(refreshCookieName, "", -1, "/api/v1/auth", "", false, true)
	c.JSON(http.StatusOK, gin.H{"ok": true})
}

func (h *AuthHandler) Me(c *gin.Context) {
	userID, ok := c.Get(middleware.ContextUserIDKey)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
		return
	}

	user, err := h.authService.Me(userID.(string))
	if err != nil {
		if errors.Is(err, service.ErrUserNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"error": "资源不存在"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"id":                   user.ID,
		"username":             user.Username,
		"is_admin":             user.IsAdmin,
		"must_change_password": user.MustChangePassword,
	})
}

func (h *AuthHandler) ChangePassword(c *gin.Context) {
	userID, ok := c.Get(middleware.ContextUserIDKey)
	if !ok {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
		return
	}

	var req changePasswordRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	if err := h.authService.ChangePassword(userID.(string), req.CurrentPassword, req.NewPassword); err != nil {
		switch {
		case errors.Is(err, service.ErrInvalidPassword):
			c.JSON(http.StatusBadRequest, gin.H{"error": "当前密码错误"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{"ok": true})
}

func setRefreshCookie(c *gin.Context, token string, remember bool) {
	maxAge := int((24 * time.Hour).Seconds()) // default 1 day
	if remember {
		maxAge = int((30 * 24 * time.Hour).Seconds())
	}
	c.SetCookie(refreshCookieName, token, maxAge, "/api/v1/auth", "", false, true)
}
