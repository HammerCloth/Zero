package handler

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/model"
	"zero/backend/internal/service"
)

type SnapshotHandler struct {
	snapshotService *service.SnapshotService
}

func NewSnapshotHandler(snapshotService *service.SnapshotService) *SnapshotHandler {
	return &SnapshotHandler{snapshotService: snapshotService}
}

type snapshotItemRequest struct {
	AccountID string  `json:"account_id" binding:"required"`
	Balance   float64 `json:"balance"`
}

type snapshotEventRequest struct {
	Category    model.EventCategory `json:"category" binding:"required"`
	Description string              `json:"description" binding:"required"`
	Amount      float64             `json:"amount" binding:"required"`
}

type snapshotRequest struct {
	Date   string                 `json:"date" binding:"required"`
	Note   string                 `json:"note"`
	Items  []snapshotItemRequest  `json:"items" binding:"required"`
	Events []snapshotEventRequest `json:"events"`
}

func (h *SnapshotHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("", h.List)
	rg.GET("/latest", h.GetLatest)
	rg.GET("/:id", h.Get)
	rg.POST("", h.Create)
	rg.PUT("/:id", h.Update)
	rg.DELETE("/:id", h.Delete)
}

func (h *SnapshotHandler) List(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	snapshots, err := h.snapshotService.List(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	out := make([]gin.H, 0, len(snapshots))
	for _, snap := range snapshots {
		full, err := h.snapshotService.Get(snap.ID)
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
			return
		}
		if full == nil {
			continue
		}
		out = append(out, gin.H{
			"id":         full.ID,
			"user_id":    full.UserID,
			"date":       full.Date,
			"note":       full.Note,
			"created_at": full.CreatedAt,
			"created_by": full.CreatedBy,
			"net_worth":  service.CalculateNetWorth(full.Items),
		})
	}

	c.JSON(http.StatusOK, gin.H{"snapshots": out})
}

func (h *SnapshotHandler) Get(c *gin.Context) {
	snapshot, err := h.snapshotService.Get(c.Param("id"))
	if err != nil {
		if errors.Is(err, service.ErrSnapshotNotFound) {
			c.JSON(http.StatusNotFound, gin.H{"error": "快照不存在"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"snapshot":  snapshot,
		"net_worth": service.CalculateNetWorth(snapshot.Items),
	})
}

func (h *SnapshotHandler) GetLatest(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	snapshot, err := h.snapshotService.GetLatest(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	if snapshot == nil {
		c.JSON(http.StatusOK, gin.H{"snapshot": nil})
		return
	}
	c.JSON(http.StatusOK, gin.H{
		"snapshot":  snapshot,
		"net_worth": service.CalculateNetWorth(snapshot.Items),
	})
}

func (h *SnapshotHandler) Create(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	var req snapshotRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	items, events := toSnapshotModels(req)
	snapshot, err := h.snapshotService.Create(userID, userID, req.Date, req.Note, items, events)
	if err != nil {
		switch {
		case errors.Is(err, service.ErrMissingBalances):
			c.JSON(http.StatusBadRequest, gin.H{"error": "存在未填写余额的账户"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}

	c.JSON(http.StatusCreated, gin.H{
		"snapshot":  snapshot,
		"net_worth": service.CalculateNetWorth(snapshot.Items),
	})
}

func (h *SnapshotHandler) Update(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	var req snapshotRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "参数不合法"})
		return
	}

	items, events := toSnapshotModels(req)
	snapshot, err := h.snapshotService.Update(c.Param("id"), userID, req.Date, req.Note, items, events)
	if err != nil {
		switch {
		case errors.Is(err, service.ErrSnapshotNotFound):
			c.JSON(http.StatusNotFound, gin.H{"error": "快照不存在"})
		case errors.Is(err, service.ErrMissingBalances):
			c.JSON(http.StatusBadRequest, gin.H{"error": "存在未填写余额的账户"})
		default:
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		}
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"snapshot":  snapshot,
		"net_worth": service.CalculateNetWorth(snapshot.Items),
	})
}

func (h *SnapshotHandler) Delete(c *gin.Context) {
	if err := h.snapshotService.Delete(c.Param("id")); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"ok": true})
}

func toSnapshotModels(req snapshotRequest) ([]model.SnapshotItem, []model.Event) {
	items := make([]model.SnapshotItem, 0, len(req.Items))
	for _, item := range req.Items {
		items = append(items, model.SnapshotItem{
			AccountID: item.AccountID,
			Balance:   item.Balance,
		})
	}

	events := make([]model.Event, 0, len(req.Events))
	for _, event := range req.Events {
		events = append(events, model.Event{
			Category:    event.Category,
			Description: event.Description,
			Amount:      event.Amount,
		})
	}
	return items, events
}
