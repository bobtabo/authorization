/**
 * 招待ドメイン エンティティモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 招待のドメインエンティティ。 */
export interface Invitation {
  id: number;
  token: string;
  role: number;
  createdAt: Date | null;
  updatedAt: Date | null;
}
