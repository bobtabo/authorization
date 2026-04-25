/**
 * 招待ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { randomBytes } from "crypto";
import { notFound } from "../../lib/errors.js";
import type { InvitationRepository } from "../../domain/invitation/repository.js";
import type { InvitationResult } from "../../domain/invitation/valueObjects.js";
import { config } from "../../config.js";

function buildResult(token: string): InvitationResult {
  const url = `${config.app.frontendUrl}/invitation/${token}`;
  return { token, url, displayUrl: url.replace(/^https?:\/\//, "") };
}

/** 招待のユースケース実装。 */
export class InvitationInteractor {
  constructor(private readonly repo: InvitationRepository) {}

  /**
   * 最新の招待情報の VO を返します。
   * @returns InvitationResult
   * @throws AppError 招待が存在しない場合
   */
  async current(): Promise<InvitationResult> {
    const inv = await this.repo.getCurrent();
    if (!inv) throw notFound("invitation_not_found");
    return buildResult(inv.token);
  }

  /**
   * 新しい招待トークンを発行し、VO を返します。
   * @returns InvitationResult
   */
  async issue(): Promise<InvitationResult> {
    const token = randomBytes(16).toString("hex");
    const inv = await this.repo.issue(token);
    return buildResult(inv.token);
  }

  /**
   * トークンで招待情報の VO を返します。
   * @param token - 招待トークン
   * @returns InvitationResult
   * @throws AppError 招待が存在しない場合
   */
  async findByToken(token: string): Promise<InvitationResult> {
    const inv = await this.repo.findByToken(token);
    if (!inv) throw notFound("invitation_not_found");
    return buildResult(inv.token);
  }
}
