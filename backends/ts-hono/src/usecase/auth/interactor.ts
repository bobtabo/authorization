/**
 * 認証ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { forbidden } from "../../lib/errors.js";
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { InvitationAuthRepository } from "../../domain/invitation/authRepository.js";
import type { StaffVo } from "../../domain/staff/valueObjects.js";
import type { LoginInput } from "./dto.js";
import { mapper } from "../../support/mapper.js";
import { StaffSymbol, StaffVoSymbol } from "../../support/mappers/index.js";

/** 認証のユースケース実装。 */
export class AuthInteractor {
  constructor(
    private readonly repo: StaffRepository,
    private readonly invitationAuthRepo: InvitationAuthRepository,
  ) {}

  /**
   * スタッフIDでログイン中スタッフの VO を返します。
   * @param staffId - スタッフID
   * @returns StaffVo、または undefined
   */
  async findUser(staffId: number): Promise<StaffVo | undefined> {
    const staff = await this.repo.findById(staffId);
    return staff ? mapper.map(staff, StaffSymbol, StaffVoSymbol) : undefined;
  }

  /**
   * OAuth 情報でスタッフを upsert してログインし、VO を返します。
   * @param input - ログイン入力
   * @returns StaffVo
   */
  async login(input: LoginInput): Promise<StaffVo> {
    const existing = await this.repo.findByProvider(input.provider, input.providerId);
    if (existing) {
      const staff = await this.repo.upsert({
        provider: input.provider,
        providerId: input.providerId,
        name: input.name,
        email: input.email,
        avatar: input.avatar ?? null,
        role: existing.role ?? 0,
        lastLoginAt: new Date(),
      });
      return mapper.map(staff, StaffSymbol, StaffVoSymbol);
    }

    const token = input.invitationToken ?? "";
    const roleValue = token ? await this.invitationAuthRepo.find(token) : null;
    if (!token || roleValue === null) {
      throw forbidden("invitation_required");
    }
    await this.invitationAuthRepo.remove(token);

    const staff = await this.repo.upsert({
      provider: input.provider,
      providerId: input.providerId,
      name: input.name,
      email: input.email,
      avatar: input.avatar ?? null,
      role: roleValue,
      lastLoginAt: new Date(),
    });
    return mapper.map(staff, StaffSymbol, StaffVoSymbol);
  }
}
