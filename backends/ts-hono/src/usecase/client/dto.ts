export interface ClientStoreInput {
  name: string;
  identifier: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
}

export interface ClientUpdateInput {
  name?: string;
  postCode?: string;
  pref?: string;
  city?: string;
  address?: string;
  building?: string;
  tel?: string;
  email?: string;
  status?: number;
}
