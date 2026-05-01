/**
 * 通知ドメイン バリューオブジェクトモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 通知一覧アイテム VO（"YYYY-MM-DD HH:MM" 形式の日時文字列を持つ）。 */
export interface NotificationItem {
  id: number;
  staffId: number;
  messageType: number;
  title: string;
  message: string;
  url: string | null;
  read: boolean;
  createdAt: string;
  updatedAt: string;
}

/** カーソルページング通知一覧 VO。 */
export interface NotificationPage {
  items: NotificationItem[];
  nextCursor: string | null;
}

/** 通知件数 VO。 */
export interface NotificationCounts {
  unread: number;
  total: number;
}
