package main

import (
	"log"

	"zero/backend/internal/config"
	"zero/backend/internal/handler"
	"zero/backend/internal/repository"
)

func main() {
	cfg := config.Load()

	db, err := repository.InitDB(cfg.DatabasePath)
	if err != nil {
		log.Fatalf("init database: %v", err)
	}
	defer db.Close()

	router, err := handler.NewRouter(cfg, db)
	if err != nil {
		log.Fatalf("build router: %v", err)
	}

	log.Printf("zero backend started on :%s (%s)", cfg.AppPort, cfg.AppEnv)
	if err := router.Run(":" + cfg.AppPort); err != nil {
		log.Fatalf("run server: %v", err)
	}
}
