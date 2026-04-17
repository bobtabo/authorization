import type { Client } from "./entity.js";

export interface ClientRepository {
  findAll(keyword?: string, status?: number): Promise<Client[]>;
  findById(id: number): Promise<Client | undefined>;
  findByToken(token: string): Promise<Client | undefined>;
  findByIdentifier(identifier: string): Promise<Client | undefined>;
  insert(data: Omit<Client, "id" | "createdAt" | "updatedAt">): Promise<Client>;
  update(id: number, data: Partial<Omit<Client, "id">>): Promise<void>;
  softDelete(id: number): Promise<void>;
}
