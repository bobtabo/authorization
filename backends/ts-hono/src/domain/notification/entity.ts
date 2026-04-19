export interface Notification {
  id: number;
  staffId: number;
  messageType: number;
  title: string;
  message: string;
  url: string | null;
  read: boolean | null;
  createdAt: Date | null;
  createdBy: number;
  updatedAt: Date | null;
  updatedBy: number;
  version: number;
}
