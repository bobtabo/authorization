/**
 * スタッフドメイン 列挙定数モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */

/** スタッフロール定数。 */
export const StaffRole = {
  MEMBER: 0,
  ADMIN: 1,
} as const;

export type StaffRoleValue = typeof StaffRole[keyof typeof StaffRole];

/** OAuth プロバイダー定数。 */
export const Provider = {
  GOOGLE: 1,
  GITHUB: 2,
} as const;

export type ProviderValue = typeof Provider[keyof typeof Provider];
