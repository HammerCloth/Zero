package handler

import (
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/service"
)

type EventHandler struct {
	eventService *service.EventService
}

func NewEventHandler(eventService *service.EventService) *EventHandler {
	return &EventHandler{eventService: eventService}
}

func (h *EventHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("/stats", h.StatsByYear)
}

func (h *EventHandler) StatsByYear(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	year := time.Now().Year()

	if yearParam := c.Query("year"); yearParam != "" {
		parsed, err := strconv.Atoi(yearParam)
		if err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": "年份参数不合法"})
			return
		}
		year = parsed
	}

	result, err := h.eventService.StatsByYear(userID, year)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"year":        year,
		"by_category": result.ByCategory,
		"grand_total": result.GrandTotal,
	})
}
