package handler

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/model"
	"zero/backend/internal/service"
)

type AccountHandler struct {
	accountService *service.AccountService
}

func NewAccountHandler(accountService *service.AccountService) *AccountHandler {
	return &AccountHandler{accountService: accountService}
}

type createAccountRequest struct {
	Name  string             `json:"name" binding:"required"`
	Type  model.AccountType  `json:"type" binding:"required"`
	Owner model.AccountOwner `json:"owner" binding:"required"`
}

type updateAccountRequest struct {
	Name      string             `json:"name" binding:"required"`
	Type      model.AccountType  `json:"type" binding:"required"`
	Owner     model.AccountOwner `json:"owner" binding:"required"`
	SortOrder int                `json:"sort_order"`
}

type reorderAccountsRequest struct {
	AccountIDs []string `json:"account_ids" binding:"required"`
}

func (h *AccountHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("", h.List)
	rg.POST("", h.Create)
	rg.PUT("/:id", h.Update)
	rg.DELETE("/:id", h.Deactivate)
	rg.PUT("/reorder", h.Reorder)
}

func (h *AccountHandler) List(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	accounts, err := h.accountService.List(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"accounts": accounts})
}

func (h *AccountHandler) Create(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	var req createAccountRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	account, err := h.accountService.Create(userID, req.Name, req.Type, req.Owner)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{"account": account})
}

func (h *AccountHandler) Update(c *gin.Context) {
	var req updateAccountRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	err := h.accountService.Update(c.Param("id"), req.Name, req.Type, req.Owner, req.SortOrder)
	if err != nil {
		switch {
		case errors.Is(err, service.ErrAccountNotFound):
			c.JSON(http.StatusNotFound, gin.H{"error": "账户不存在"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true})
}

func (h *AccountHandler) Deactivate(c *gin.Context) {
	err := h.accountService.Deactivate(c.Param("id"))
	if err != nil {
		switch {
		case errors.Is(err, service.ErrAccountNotFound):
			c.JSON(http.StatusNotFound, gin.H{"error": "账户不存在"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true})
}

func (h *AccountHandler) Reorder(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	var req reorderAccountsRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	if err := h.accountService.Reorder(userID, req.AccountIDs); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true})
}
