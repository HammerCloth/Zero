package handler

import (
	"net/http"
	"strconv"
	"time"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/service"
)

type DashboardHandler struct {
	dashboardService *service.DashboardService
}

func NewDashboardHandler(dashboardService *service.DashboardService) *DashboardHandler {
	return &DashboardHandler{dashboardService: dashboardService}
}

func (h *DashboardHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("/summary", h.Summary)
	rg.GET("/trend", h.Trend)
	rg.GET("/composition", h.Composition)
	rg.GET("/monthly-growth", h.MonthlyGrowth)
}

func (h *DashboardHandler) Summary(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	result, err := h.dashboardService.Summary(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, result)
}

func (h *DashboardHandler) Trend(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	rangeKey := c.DefaultQuery("range", "all")

	points, err := h.dashboardService.Trend(userID, rangeKey)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"points": points})
}

func (h *DashboardHandler) Composition(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	result, err := h.dashboardService.Composition(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, result)
}

func (h *DashboardHandler) MonthlyGrowth(c *gin.Context) {
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

	result, err := h.dashboardService.MonthlyGrowth(userID, year)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"points": result, "year": year})
}
