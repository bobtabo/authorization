export interface NotificationPage {
  items: import("./entity.js").Notification[];
  nextCursor: string | null;
}

export interface NotificationCounts {
  unread: number;
  total: number;
}
