export const ClientStatus = {
  INACTIVE: 0,
  ACTIVE: 1,
  RUNNING: 2,
  STOPPED: 3,
  DELETED: 4,
} as const;

export type ClientStatusValue = typeof ClientStatus[keyof typeof ClientStatus];
