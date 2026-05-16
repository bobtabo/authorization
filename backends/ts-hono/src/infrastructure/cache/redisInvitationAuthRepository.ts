/**
 * 招待認証 Redis キャッシュリポジトリモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { redis } from "../../lib/redis.js";
import { config } from "../../config.js";
import type { InvitationAuthRepository } from "../../domain/invitation/authRepository.js";

function cacheKey(token: string): string {
  return `${config.app.cachePrefix}:invitation_auth:invitation_auth:${token}`;
}

/** Redis を用いた招待認証キャッシュリポジトリ。 */
export class RedisInvitationAuthRepository implements InvitationAuthRepository {
  async store(token: string, role: number, ttl: number): Promise<void> {
    await redis.setex(cacheKey(token), ttl, String(role));
  }

  async find(token: string): Promise<number | null> {
    const val = await redis.get(cacheKey(token));
    if (val === null) return null;
    const n = parseInt(val, 10);
    return isNaN(n) ? null : n;
  }

  async remove(token: string): Promise<void> {
    await redis.del(cacheKey(token));
  }
}
