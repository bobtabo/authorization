export interface NotificationBulkReadInput {
  executorId: number;
  ids: number[];
  allFlag: boolean;
}

export interface NotificationFanOutInput {
  title: string;
  body?: string;
}

export interface NotificationPatchInput {
  id: number;
  read?: boolean;
  title?: string;
  message?: string;
}
