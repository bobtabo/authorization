/**
 * クライアントドメイン 列挙定数モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** クライアントステータス定数。 */
export const ClientStatus = {
  INACTIVE: 0,
  ACTIVE: 1,
  RUNNING: 2,
  STOPPED: 3,
  DELETED: 4,
} as const;

export type ClientStatusValue = typeof ClientStatus[keyof typeof ClientStatus];
