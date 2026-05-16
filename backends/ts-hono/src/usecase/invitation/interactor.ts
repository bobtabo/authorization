/**
 * 招待ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { randomBytes } from "crypto";
import { notFound } from "../../lib/errors.js";
import type { InvitationRepository } from "../../domain/invitation/repository.js";
import type { InvitationAuthRepository } from "../../domain/invitation/authRepository.js";
import type { InvitationResult } from "../../domain/invitation/valueObjects.js";
import { config } from "../../config.js";

function buildResult(token: string): InvitationResult {
  const url = `${config.app.frontendUrl}/invitation/${token}`;
  return { token, url, displayUrl: url.replace(/^https?:\/\//, "") };
}

/** 招待のユースケース実装。 */
export class InvitationInteractor {
  constructor(
    private readonly repo: InvitationRepository,
    private readonly authRepo: InvitationAuthRepository,
  ) {}

  /**
   * 最新の招待情報の VO を返します。
   * @param role - ロール (1=admin, 2=member)
   * @returns InvitationResult
   * @throws AppError 招待が存在しない場合
   */
  async current(role: number): Promise<InvitationResult> {
    const inv = await this.repo.getCurrentByRole(role);
    if (!inv) throw notFound("invitation_not_found");
    return buildResult(inv.token);
  }

  /**
   * 新しい招待トークンを発行し、VO を返します。
   * @param role - ロール (1=admin, 2=member)
   * @returns InvitationResult
   */
  async issue(role: number): Promise<InvitationResult> {
    const token = randomBytes(16).toString("hex");
    const inv = await this.repo.issue(token, role);
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
    await this.authRepo.store(inv.token, inv.role, 600);
    return buildResult(inv.token);
  }
}
