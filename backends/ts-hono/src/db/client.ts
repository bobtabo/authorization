/**
 * データベース接続モジュール。
 *
 * @author Satoshi Nagashiba <satoshi.nagashiba@gmail.com>
 */
import { drizzle } from "drizzle-orm/mysql2";
import mysql from "mysql2/promise";
import { config } from "../config.js";

const pool = mysql.createPool({
  host: config.db.host,
  port: config.db.port,
  database: config.db.database,
  user: config.db.user,
  password: config.db.password,
  charset: "utf8mb4",
  waitForConnections: true,
  connectionLimit: 10,
});

export const db = drizzle(pool);

/** Drizzle DB インスタンスの型。リポジトリのコンストラクタ引数に使用します。 */
export type DB = typeof db;

/**
 * Drizzle トランザクションを DB 型にキャストするヘルパー。
 * `db.transaction` コールバックで受け取った `tx` をリポジトリに渡す際に使用します。
 */
export function asTx(tx: unknown): DB {
  return tx as DB;
}
