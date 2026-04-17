export interface Client {
  id: number;
  name: string;
  identifier: string;
  postCode: string | null;
  pref: string | null;
  city: string | null;
  address: string | null;
  building: string | null;
  tel: string | null;
  email: string | null;
  status: number | null;
  token: string | null;
  publicKey: string | null;
  privateKey: string | null;
  fingerprint: string | null;
  startedAt: Date | null;
  stoppedAt: Date | null;
  createdAt: Date | null;
  updatedAt: Date | null;
  deletedAt: Date | null;
}
