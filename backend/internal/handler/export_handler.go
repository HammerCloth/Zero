package handler

import (
	"net/http"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/middleware"
	"zero/backend/internal/service"
)

type ExportHandler struct {
	exportService *service.ExportService
}

func NewExportHandler(exportService *service.ExportService) *ExportHandler {
	return &ExportHandler{exportService: exportService}
}

func (h *ExportHandler) RegisterRoutes(rg *gin.RouterGroup) {
	rg.GET("/csv", h.ExportCSV)
}

func (h *ExportHandler) ExportCSV(c *gin.Context) {
	userID := c.GetString(middleware.ContextUserIDKey)
	content, err := h.exportService.ExportSnapshotsCSV(userID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.Header("Content-Type", "text/csv; charset=utf-8")
	c.Header("Content-Disposition", `attachment; filename="snapshots.csv"`)
	c.Data(http.StatusOK, "text/csv; charset=utf-8", content)
}
