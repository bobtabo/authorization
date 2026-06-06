/**
 * クライアントドメイン リポジトリインターフェースモジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import type { Client } from "./entity.js";

/** ページング付き検索条件。 */
export interface FindAllOptions {
  keyword?: string;
  status?: number;
  offset?: number;
  limit?: number;
  sort?: string;
  sortType?: string;
}

/** クライアントのリポジトリインターフェース。 */
export interface ClientRepository {
  findAll(keyword?: string, status?: number, options?: FindAllOptions): Promise<Client[]>;
  countAll(keyword?: string, status?: number): Promise<number>;
  findById(id: number): Promise<Client | undefined>;
  findByToken(token: string): Promise<Client | undefined>;
  findByIdentifier(identifier: string): Promise<Client | undefined>;
  insert(data: Omit<Client, "id" | "createdAt" | "updatedAt">): Promise<Client>;
  update(id: number, data: Partial<Omit<Client, "id">>, version: number): Promise<void>;
  softDelete(id: number, version: number): Promise<void>;
}
