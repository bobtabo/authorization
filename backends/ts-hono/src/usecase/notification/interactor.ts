/**
 * 通知ユースケース Interactor モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { notFound, unauthorized, badRequest } from "../../lib/errors.js";
import type { NotificationRepository } from "../../domain/notification/repository.js";
import type { StaffRepository } from "../../domain/staff/repository.js";
import type { Notification } from "../../domain/notification/entity.js";
import type { NotificationPage, NotificationCounts, NotificationItem } from "../../domain/notification/valueObjects.js";
import { formatTime } from "../../lib/cookie.js";

function toItem(n: Notification): NotificationItem {
  return {
    id: n.id, staffId: n.staffId, messageType: n.messageType,
    title: n.title, message: n.message, url: n.url ?? null,
    read: n.read ?? false,
    createdAt: formatTime(n.createdAt) ?? "",
    updatedAt: formatTime(n.updatedAt) ?? "",
  };
}

/** 通知のユースケース実装。 */
export class NotificationInteractor {
  constructor(
    private readonly repo: NotificationRepository,
    private readonly staffRepo: StaffRepository,
  ) {}

  /**
   * カーソルページングで通知一覧の VO を返します。
   * @param staffId - スタッフID
   * @param cursor - ページカーソル（undefined で先頭から）
   * @param limit - 取得件数
   * @returns NotificationPage
   */
  async listPage(staffId: number, cursor: string | undefined, limit: number): Promise<NotificationPage> {
    const { items, nextCursor } = await this.repo.listPage(staffId, cursor, limit);
    return { items: items.map(toItem), nextCursor };
  }

  /**
   * スタッフの未読・全体通知数を返します。
   * @param staffId - スタッフID
   * @returns NotificationCounts
   */
  async countNotifications(staffId: number): Promise<NotificationCounts> {
    return this.repo.count(staffId);
  }

  /**
   * スタッフの通知を一括既読にして更新件数を返します。
   * @param executorId - 操作者スタッフID
   * @param ids - 既読にする通知IDリスト
   * @param allFlag - true の場合全件既読
   * @returns 更新件数
   * @throws AppError 未認証、またはパラメーター不正の場合
   */
  async bulkRead(executorId: number, ids: number[], allFlag: boolean): Promise<number> {
    if (executorId === 0) throw unauthorized("unauthenticated");
    if (ids.length === 0 && !allFlag) throw badRequest("ids_or_all_required");
    return this.repo.bulkMarkRead(executorId, ids, allFlag);
  }

  /**
   * 全アクティブスタッフへ通知を配信します。
   * @param title - 通知タイトル
   * @param body - 通知本文
   * @param url - 通知リンク URL
   * @param executorId - 操作者スタッフID
   * @param messageType - メッセージ種別
   */
  async fanOut(title: string, body?: string, url?: string, executorId = 0, messageType = 1): Promise<void> {
    const staffs = await this.staffRepo.findAllActive();
    for (const staff of staffs) {
      await this.repo.insert({
        staffId: staff.id, messageType, title,
        message: body ?? "", url: url ?? null, read: false,
      });
    }
  }

  /**
   * 通知を既読にします。
   * @param id - 通知ID
   * @throws AppError 通知が存在しない場合
   */
  async patch(id: number, data: Partial<Pick<Notification, "read" | "title" | "message">>): Promise<void> {
    const n = await this.repo.findById(id);
    if (!n) throw notFound("notification_not_found");
    await this.repo.patch(id, data);
  }
}
