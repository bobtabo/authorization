/**
 * Gate ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { importPKCS8, importSPKI, jwtVerify, SignJWT } from "jose";
import { config } from "../../config.js";
import { unauthorized, notFound, internal } from "../../lib/errors.js";
import type { ClientRepository } from "../../domain/client/repository.js";
import type { GateIssueVo, GateVerifyVo } from "../../domain/gate/valueObjects.js";
import type { RedisGateRepository } from "../../infrastructure/cache/redisGateRepository.js";
import type { DrizzleJwtHistoryRepository } from "../../infrastructure/persistence/drizzleJwtHistoryRepository.js";

/** Gate のユースケース実装。 */
export class GateInteractor {
  constructor(
    private readonly clientRepo: ClientRepository,
    private readonly cacheRepo: RedisGateRepository,
    private readonly historyRepo?: DrizzleJwtHistoryRepository,
  ) {}

  /**
   * クライアント会員向け JWT を発行し、VO を返します。キャッシュが有効な場合はキャッシュから返します。
   * @param accessToken - クライアントアクセストークン
   * @param member - 会員ID
   * @returns GateIssueVo
   * @throws AppError クライアントが存在しない場合、または秘密鍵が未設定の場合
   */
  async issueToken(accessToken: string, member: string): Promise<GateIssueVo> {
    const client = await this.clientRepo.findByToken(accessToken);
    if (!client) throw unauthorized("invalid_token");
    if (!client.privateKey) throw internal("private_key_not_found");

    const cached = await this.cacheRepo.getJwt(client.identifier, member);
    if (cached) return { token: cached };

    const token = await this.issueJwt(client.privateKey, client.identifier, member);
    await this.cacheRepo.putJwt(client.identifier, member, token);
    if (this.historyRepo && client.id) {
      await this.historyRepo.save(client.id, member, new Date(), token).catch(() => {});
    }
    return { token };
  }

  /**
   * JWT を検証し、ペイロードの VO を返します。
   * @param identifier - クライアント識別子
   * @param token - JWT 文字列
   * @returns GateVerifyVo
   * @throws AppError クライアントが存在しない場合、または JWT が無効の場合
   */
  async verify(identifier: string, token: string): Promise<GateVerifyVo> {
    const client = await this.clientRepo.findByIdentifier(identifier);
    if (!client) throw notFound("client_not_found");
    if (!client.publicKey) throw internal("public_key_not_found");

    try {
      const publicKey = await importSPKI(client.publicKey, config.jwt.algorithm);
      const { payload } = await jwtVerify(token, publicKey, {
        issuer: config.jwt.issuer,
        audience: identifier,
        algorithms: [config.jwt.algorithm],
      });
      return {
        identifier,
        member: payload.sub,
        fingerprint: client.fingerprint ?? null,
        payload: payload as Record<string, unknown>,
      };
    } catch (e) {
      throw unauthorized(e instanceof Error ? e.message : "jwt_error");
    }
  }

  private async issueJwt(privateKeyPem: string, identifier: string, member: string): Promise<string> {
    const privateKey = await importPKCS8(privateKeyPem, config.jwt.algorithm);
    const now = Math.floor(Date.now() / 1000);
    return new SignJWT({ sub: member, aud: [identifier] })
      .setProtectedHeader({ alg: config.jwt.algorithm })
      .setIssuer(config.jwt.issuer)
      .setIssuedAt(now)
      .setExpirationTime(now + config.jwt.ttl)
      .sign(privateKey);
  }
}
