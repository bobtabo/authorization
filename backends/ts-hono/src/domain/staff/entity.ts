export interface Staff {
  id: number;
  name: string;
  email: string;
  provider: number;
  providerId: string;
  avatar: string | null;
  role: number | null;
  createdAt: Date | null;
  updatedAt: Date | null;
  deletedAt: Date | null;
}
