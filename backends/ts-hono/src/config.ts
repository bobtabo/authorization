import { config as loadEnv } from "dotenv";

const envFile = process.env.ENV_FILE || ".env";
loadEnv({ path: envFile });

function env(key: string, fallback = ""): string {
  return process.env[key] ?? fallback;
}
function envInt(key: string, fallback: number): number {
  const v = process.env[key];
  return v ? parseInt(v, 10) : fallback;
}

export const config = {
  app: {
    env: env("APP_ENV", "local"),
    port: envInt("APP_PORT", 3000),
    frontendUrl: env("FRONTEND_URL", "http://localhost:3000"),
    staffCookieLifetime: envInt("STAFF_COOKIE_LIFETIME", 60),
    notificationDefaultLimit: envInt("NOTIFICATION_DEFAULT_LIMIT", 10),
    cachePrefix: env("CACHE_PREFIX", ""),
  },
  db: {
    host: env("DB_HOST", "localhost"),
    port: envInt("DB_PORT", 3306),
    database: env("DB_DATABASE", "authorization"),
    user: env("DB_USERNAME", "root"),
    password: env("DB_PASSWORD", ""),
  },
  redis: {
    host: env("REDIS_HOST", "localhost"),
    port: envInt("REDIS_PORT", 6379),
    password: env("REDIS_PASSWORD") || undefined,
    db: envInt("REDIS_DB", 0),
  },
  oauth: {
    googleClientId: env("GOOGLE_CLIENT_ID"),
    googleClientSecret: env("GOOGLE_CLIENT_SECRET"),
    googleRedirectUrl: env("GOOGLE_REDIRECT_URL"),
  },
  jwt: {
    issuer: "authorization",
    algorithm: "RS256" as const,
    ttl: 1800,
    cacheTtl: envInt("GATE_JWT_CACHE_TTL", 1800),
  },
} as const;
