import { redis } from "../lib/redis.js";
import { config } from "../config.js";

function cacheKey(identifier: string, member: string): string {
  return `${config.app.cachePrefix}:gate:${identifier}:${member}`;
}

export async function getJwt(identifier: string, member: string): Promise<string | null> {
  return redis.get(cacheKey(identifier, member));
}

export async function putJwt(identifier: string, member: string, token: string): Promise<void> {
  await redis.setex(cacheKey(identifier, member), config.jwt.cacheTtl, token);
}
