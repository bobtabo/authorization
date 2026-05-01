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
  async store(token: string, ttl: number): Promise<void> {
    await redis.setex(cacheKey(token), ttl, token);
  }

  async find(token: string): Promise<string | null> {
    return redis.get(cacheKey(token));
  }

  async remove(token: string): Promise<void> {
    await redis.del(cacheKey(token));
  }
}
