/**
 * 認証ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { StaffVo } from "../../domain/staff/valueObjects.js";
import type { LoginInput } from "./dto.js";

function toStaffVo(staff: { id: number; name: string; avatar: string | null | undefined; role: number | null }): StaffVo {
  return { id: staff.id, name: staff.name, avatar: staff.avatar ?? null, role: staff.role ?? 0 };
}

/** 認証のユースケース実装。 */
export class AuthInteractor {
  constructor(private readonly repo: StaffRepository) {}

  /**
   * スタッフIDでログイン中スタッフの VO を返します。
   * @param staffId - スタッフID
   * @returns StaffVo、または undefined
   */
  async findUser(staffId: number): Promise<StaffVo | undefined> {
    const staff = await this.repo.findById(staffId);
    return staff ? toStaffVo(staff) : undefined;
  }

  /**
   * OAuth 情報でスタッフを upsert してログインし、VO を返します。
   * @param input - ログイン入力
   * @returns StaffVo
   */
  async login(input: LoginInput): Promise<StaffVo> {
    const staff = await this.repo.upsert({
      provider: input.provider,
      providerId: input.providerId,
      name: input.name,
      email: input.email,
      avatar: input.avatar ?? null,
      role: 0,
    });
    return toStaffVo(staff);
  }
}
