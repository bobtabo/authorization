/**
 * ゲートインフラ Redis キャッシュリポジトリモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { redis } from "../../lib/redis.js";
import { config } from "../../config.js";

function cacheKey(identifier: string, member: string): string {
  return `${config.app.cachePrefix}:gate:${identifier}:${member}`;
}

/** Redis を用いたゲート JWT キャッシュリポジトリ。 */
export class RedisGateRepository {
  /**
   * キャッシュから JWT を取得します。
   * @param identifier - クライアント識別子
   * @param member - メンバー識別子
   * @returns JWT 文字列、またはキャッシュミス時 null
   */
  async getJwt(identifier: string, member: string): Promise<string | null> {
    return redis.get(cacheKey(identifier, member));
  }

  /**
   * JWT をキャッシュに保存します。
   * @param identifier - クライアント識別子
   * @param member - メンバー識別子
   * @param token - JWT 文字列
   */
  async putJwt(identifier: string, member: string, token: string): Promise<void> {
    await redis.setex(cacheKey(identifier, member), config.jwt.cacheTtl, token);
  }
}
