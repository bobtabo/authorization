export const StaffRole = {
  MEMBER: 0,
  ADMIN: 1,
} as const;

export type StaffRoleValue = typeof StaffRole[keyof typeof StaffRole];

export const Provider = {
  GOOGLE: 1,
} as const;

export type ProviderValue = typeof Provider[keyof typeof Provider];
