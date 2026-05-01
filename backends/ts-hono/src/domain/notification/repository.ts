/**
 * 通知ドメイン リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Notification } from "./entity.js";
import type { NotificationCounts } from "./valueObjects.js";

export interface NotificationRepository {
  /**
   * カーソルページングで通知エンティティ一覧と次カーソルを返します。
   * @param staffId - スタッフID
   * @param cursor - ページカーソル（undefined で先頭から）
   * @param limit - 取得件数
   */
  listPage(staffId: number, cursor: string | undefined, limit: number): Promise<{ items: Notification[]; nextCursor: string | null }>;

  /**
   * スタッフの未読・全体通知数を返します。
   * @param staffId - スタッフID
   */
  count(staffId: number): Promise<NotificationCounts>;

  /**
   * 対象通知を一括既読にして更新件数を返します。
   * @param executorId - 操作者スタッフID
   * @param ids - 既読にする通知IDリスト（allFlag=true の場合無視）
   * @param allFlag - true の場合全件既読
   */
  bulkMarkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number>;

  /**
   * 通知を新規登録します。
   * @param data - 通知データ
   */
  insert(data: Omit<Notification, "id" | "createdAt" | "updatedAt" | "createdBy" | "updatedBy" | "version">): Promise<void>;

  /**
   * IDで通知エンティティを返します。存在しない場合は undefined を返します。
   * @param id - 通知ID
   */
  findById(id: number): Promise<Notification | undefined>;

  /**
   * 通知を部分更新します。
   * @param id - 通知ID
   * @param data - 更新データ
   */
  patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>): Promise<void>;
}
