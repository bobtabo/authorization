/**
 * スタッフユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { badRequest, notFound } from "../../lib/errors.js";
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { StaffListItem } from "../../domain/staff/valueObjects.js";
import { mapper } from "../../support/mapper.js";
import { StaffSymbol, StaffListItemSymbol } from "../../support/mappers/index.js";

/** スタッフのユースケース実装。 */
export class StaffInteractor {
  constructor(private readonly repo: StaffRepository) {}

  /**
   * 検索条件に合致するスタッフ一覧の VO と総件数を返します。
   * @param keyword - キーワード検索
   * @param roles - ロールフィルター
   * @param offset - オフセット
   * @param limit - 取得件数
   * @param sort - ソート対象
   * @param sortType - ソート順
   * @returns StaffListItem の配列と総件数のタプル
   */
  async findByCondition(keyword?: string, roles?: number[], offset = 0, limit = 10, sort?: string, sortType?: string): Promise<[StaffListItem[], number]> {
    const count = await this.repo.countAll(keyword, roles);
    const staffs = await this.repo.findAll(keyword, roles, { offset, limit, sort, sortType });
    return [mapper.mapArray(staffs, StaffSymbol, StaffListItemSymbol), count];
  }

  /**
   * スタッフの権限を更新します。
   * @param staffId - スタッフID
   * @param role - 新しい権限値
   * @param executorId - 操作者スタッフID
   * @throws AppError 自分自身のロール更新、またはスタッフが存在しない場合
   */
  async updateRole(staffId: number, role: number, executorId: number): Promise<void> {
    if (staffId === executorId) throw badRequest("cannot_update_own_role");
    const staff = await this.repo.findById(staffId);
    if (!staff) throw notFound("staff_not_found");
    await this.repo.updateRole(staffId, role, staff.version);
  }

  /**
   * スタッフの論理削除を復元します。
   * @param staffId - スタッフID
   * @throws AppError スタッフが存在しない場合
   */
  async restore(staffId: number): Promise<void> {
    const staff = await this.repo.findByIdUnscoped(staffId);
    if (!staff) throw notFound("staff_not_found");
    await this.repo.restore(staffId, staff.version);
  }

  /**
   * スタッフを論理削除します。
   * @param staffId - スタッフID
   * @param executorId - 操作者スタッフID
   * @throws AppError 自分自身の削除、またはスタッフが存在しない場合
   */
  async destroy(staffId: number, executorId: number): Promise<void> {
    if (staffId === executorId) throw badRequest("cannot_delete_self");
    const staff = await this.repo.findById(staffId);
    if (!staff) throw notFound("staff_not_found");
    await this.repo.softDelete(staffId, staff.version);
  }
}
