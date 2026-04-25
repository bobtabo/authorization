/**
 * 招待ドメイン リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Invitation } from "./entity.js";

/** 招待のリポジトリインターフェース。 */
export interface InvitationRepository {
  getCurrent(): Promise<Invitation | undefined>;
  issue(token: string): Promise<Invitation>;
  findByToken(token: string): Promise<Invitation | undefined>;
}
