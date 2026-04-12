package config

import (
	"fmt"
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

type Config struct {
	App   AppConfig
	DB    DBConfig
	Redis RedisConfig
	OAuth OAuthConfig
	JWT   JWTConfig
}

type AppConfig struct {
	Env                      string
	Port                     string
	FrontendURL              string
	StaffCookieLifetime      int // 分
	NotificationDefaultLimit int
	CachePrefix              string
}

type DBConfig struct {
	DSN string
}

type RedisConfig struct {
	Addr     string
	Password string
	DB       int
}

type OAuthConfig struct {
	GoogleClientID     string
	GoogleClientSecret string
	GoogleRedirectURL  string
}

type JWTConfig struct {
	Issuer    string
	Algorithm string
	TTL       int // 秒
	CacheTTL  int // 秒
}

func Load() *Config {
	_ = godotenv.Load()

	return &Config{
		App: AppConfig{
			Env:                      getEnv("APP_ENV", "local"),
			Port:                     getEnv("APP_PORT", "8080"),
			FrontendURL:              getEnv("FRONTEND_URL", "http://localhost:5173"),
			StaffCookieLifetime:      getEnvInt("STAFF_COOKIE_LIFETIME", 60),
			NotificationDefaultLimit: getEnvInt("NOTIFICATION_DEFAULT_LIMIT", 10),
			CachePrefix:              getEnv("CACHE_PREFIX", ""),
		},
		DB: DBConfig{
			DSN: buildDSN(),
		},
		Redis: RedisConfig{
			Addr:     getEnv("REDIS_HOST", "localhost") + ":" + getEnv("REDIS_PORT", "6379"),
			Password: getEnv("REDIS_PASSWORD", ""),
			DB:       getEnvInt("REDIS_DB", 0),
		},
		OAuth: OAuthConfig{
			GoogleClientID:     getEnv("GOOGLE_CLIENT_ID", ""),
			GoogleClientSecret: getEnv("GOOGLE_CLIENT_SECRET", ""),
			GoogleRedirectURL:  getEnv("GOOGLE_REDIRECT_URL", ""),
		},
		JWT: JWTConfig{
			Issuer:    "authorization",
			Algorithm: "RS256",
			TTL:       1800,
			CacheTTL:  getEnvInt("GATE_JWT_CACHE_TTL", 1800),
		},
	}
}

func buildDSN() string {
	host := getEnv("DB_HOST", "localhost")
	port := getEnv("DB_PORT", "3306")
	user := getEnv("DB_USERNAME", "root")
	pass := getEnv("DB_PASSWORD", "")
	name := getEnv("DB_DATABASE", "authorization")
	return fmt.Sprintf("%s:%s@tcp(%s:%s)/%s?charset=utf8mb4&parseTime=True&loc=Local",
		user, pass, host, port, name)
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}

func getEnvInt(key string, fallback int) int {
	if v := os.Getenv(key); v != "" {
		if i, err := strconv.Atoi(v); err == nil {
			return i
		}
	}
	return fallback
}
