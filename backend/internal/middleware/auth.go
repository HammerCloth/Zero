package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"

	"zero/backend/internal/service"
)

const (
	ContextUserIDKey  = "user_id"
	ContextIsAdminKey = "is_admin"
)

func Auth(tokenService *service.TokenService) gin.HandlerFunc {
	return func(c *gin.Context) {
		header := c.GetHeader("Authorization")
		if header == "" {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
			c.Abort()
			return
		}

		parts := strings.SplitN(header, " ", 2)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
			c.Abort()
			return
		}

		claims, err := tokenService.ParseAccessToken(parts[1])
		if err != nil {
			c.JSON(http.StatusUnauthorized, gin.H{"error": "未登录或登录已过期"})
			c.Abort()
			return
		}

		c.Set(ContextUserIDKey, claims.UserID)
		c.Set(ContextIsAdminKey, claims.IsAdmin)
		c.Next()
	}
}

func RequireAdmin() gin.HandlerFunc {
	return func(c *gin.Context) {
		isAdmin, ok := c.Get(ContextIsAdminKey)
		if !ok || isAdmin != true {
			c.JSON(http.StatusForbidden, gin.H{"error": "无权限执行此操作"})
			c.Abort()
			return
		}
		c.Next()
	}
}
