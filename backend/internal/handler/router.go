package handler

import (
	"database/sql"
	"time"

	"github.com/gin-contrib/cors"
	"github.com/gin-gonic/gin"

	"zero/backend/internal/config"
	"zero/backend/internal/middleware"
	"zero/backend/internal/repository"
	"zero/backend/internal/service"
)

func NewRouter(cfg config.Config, db *sql.DB) (*gin.Engine, error) {
	tokenService, err := service.NewTokenService(
		cfg.AccessSecret,
		cfg.RefreshSecret,
		cfg.AccessTTL,
		cfg.RefreshTTL,
	)
	if err != nil {
		return nil, err
	}

	userRepo := repository.NewUserRepository(db)
	accountRepo := repository.NewAccountRepository(db)
	snapshotRepo := repository.NewSnapshotRepository(db)
	eventRepo := repository.NewEventRepository(db)

	authService := service.NewAuthService(userRepo, accountRepo, tokenService)
	userService := service.NewUserService(userRepo)
	accountService := service.NewAccountService(accountRepo)
	snapshotService := service.NewSnapshotService(snapshotRepo, accountRepo)
	eventService := service.NewEventService(eventRepo)
	dashboardService := service.NewDashboardService(snapshotRepo, accountRepo)
	exportService := service.NewExportService(snapshotRepo)

	limiter := middleware.NewLoginRateLimiter(5, 15*time.Minute, 15*time.Minute)

	authHandler := NewAuthHandler(authService, limiter)
	userHandler := NewUserHandler(userService)
	accountHandler := NewAccountHandler(accountService)
	snapshotHandler := NewSnapshotHandler(snapshotService)
	eventHandler := NewEventHandler(eventService)
	dashboardHandler := NewDashboardHandler(dashboardService)
	exportHandler := NewExportHandler(exportService)

	if cfg.AppEnv == "production" {
		gin.SetMode(gin.ReleaseMode)
	}

	engine := gin.New()
	if cfg.AppEnv == "production" {
		// 反代（如 Caddy）在 Docker 网段内，避免默认「信任所有代理」
		_ = engine.SetTrustedProxies([]string{"127.0.0.1", "172.16.0.0/12", "10.0.0.0/8"})
	}
	engine.Use(gin.Logger())
	engine.Use(gin.Recovery())
	engine.Use(middleware.ErrorHandler())
	engine.Use(cors.New(cors.Config{
		AllowOrigins:     cfg.FrontendOrigins,
		AllowMethods:     []string{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
		AllowHeaders:     []string{"Authorization", "Content-Type"},
		AllowCredentials: true,
		MaxAge:           12 * time.Hour,
	}))

	engine.GET("/healthz", func(c *gin.Context) {
		c.JSON(200, gin.H{"ok": true})
	})

	v1 := engine.Group("/api/v1")
	authGroup := v1.Group("/auth")
	authGroup.Use(limiter.Middleware())
	authHandler.RegisterRoutes(authGroup)

	protected := v1.Group("")
	protected.Use(middleware.Auth(tokenService))

	protectedAuthGroup := protected.Group("/auth")
	authHandler.RegisterProtectedRoutes(protectedAuthGroup)

	adminUsers := protected.Group("/users")
	adminUsers.Use(middleware.RequireAdmin())
	userHandler.RegisterRoutes(adminUsers)

	accountHandler.RegisterRoutes(protected.Group("/accounts"))
	snapshotHandler.RegisterRoutes(protected.Group("/snapshots"))
	eventHandler.RegisterRoutes(protected.Group("/events"))
	dashboardHandler.RegisterRoutes(protected.Group("/dashboard"))
	exportHandler.RegisterRoutes(protected.Group("/export"))

	return engine, nil
}
