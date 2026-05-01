/**
 * スタッフドメイン エンティティモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** スタッフのドメインエンティティ。 */
export interface Staff {
  id: number;
  name: string;
  email: string;
  provider: number;
  providerId: string;
  avatar: string | null;
  role: number | null;
  lastLoginAt: Date | null;
  createdAt: Date | null;
  createdBy: number | null;
  updatedAt: Date | null;
  updatedBy: number | null;
  deletedAt: Date | null;
  version: number;
}
