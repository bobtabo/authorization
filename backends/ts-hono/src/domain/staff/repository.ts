/**
 * スタッフドメイン リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Staff } from "./entity.js";

/** スタッフ一覧検索オプション。 */
export interface FindAllStaffOptions {
  offset?: number;
  limit?: number;
  sort?: string;
  sortType?: string;
}

/** スタッフのリポジトリインターフェース。 */
export interface StaffRepository {
  countAll(keyword?: string, roles?: number[]): Promise<number>;
  findAll(keyword?: string, roles?: number[], options?: FindAllStaffOptions): Promise<Staff[]>;
  findById(id: number): Promise<Staff | undefined>;
  findByIdUnscoped(id: number): Promise<Staff | undefined>;
  findByProvider(provider: number, providerId: string): Promise<Staff | undefined>;
  findAllActive(): Promise<Staff[]>;
  upsert(data: Omit<Staff, "id" | "createdAt" | "updatedAt" | "deletedAt" | "createdBy" | "updatedBy" | "version"> & { avatar?: string | null; role?: number | null; lastLoginAt?: Date | null }): Promise<Staff>;
  updateRole(id: number, role: number, version: number): Promise<void>;
  softDelete(id: number, version: number): Promise<void>;
  restore(id: number, version: number): Promise<void>;
}
