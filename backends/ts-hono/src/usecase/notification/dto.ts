/**
 * 通知ユースケース DTO モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** 通知一括既読入力 DTO。 */
export interface NotificationBulkReadInput {
  executorId: number;
  ids: number[];
  allFlag: boolean;
}

/** 通知ファンアウト入力 DTO。 */
export interface NotificationFanOutInput {
  title: string;
  body?: string;
}

/** 通知部分更新入力 DTO。 */
export interface NotificationPatchInput {
  id: number;
  read?: boolean;
  title?: string;
  message?: string;
}
