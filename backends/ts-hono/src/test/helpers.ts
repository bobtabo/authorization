/**
 * テストデータ生成ヘルパー。
 * 直接 mysql2 で INSERT することでアプリの DB 依存なしに使えます。
 */
import { generateKeyPairSync, randomBytes } from "crypto";
import mysql from "mysql2/promise";

const pool = mysql.createPool({
  host: process.env.DB_HOST ?? "host.docker.internal",
  port: Number(process.env.DB_PORT ?? 3306),
  database: process.env.DB_DATABASE ?? "authorization_test",
  user: process.env.DB_USERNAME ?? "develop",
  password: process.env.DB_PASSWORD ?? "docker#DOCKER1234",
  charset: "utf8mb4",
});

async function q(sql: string, params: unknown[] = []) {
  const [rows] = await pool.execute(sql, params);
  return rows as mysql.RowDataPacket[];
}

export interface TestStaff {
  id: number;
  name: string;
  email: string;
  role: number;
}

export interface TestClient {
  id: number;
  name: string;
  identifier: string;
  token: string;
  privateKey: string;
  publicKey: string;
}

export interface TestInvitation {
  id: number;
  token: string;
}

export interface TestNotification {
  id: number;
  staffId: number;
  title: string;
}

export async function makeStaff(overrides: Partial<TestStaff & { email: string }> = {}): Promise<TestStaff> {
  const data = {
    name: "テストスタッフ",
    email: `staff-${Date.now()}@example.com`,
    provider: 1,
    provider_id: String(Date.now()),
    role: 1,
    ...overrides,
  };
  const [result] = await pool.execute(
    "INSERT INTO staffs (name, email, provider, provider_id, role) VALUES (?, ?, ?, ?, ?)",
    [data.name, data.email, data.provider, data.provider_id, data.role],
  ) as mysql.ResultSetHeader[];
  return { id: result.insertId, name: data.name, email: data.email, role: data.role };
}

export async function makeClientRecord(overrides: Partial<{ identifier: string; email: string; name: string }> = {}): Promise<TestClient> {
  const { privateKey, publicKey } = generateKeyPairSync("rsa", { modulusLength: 2048 });
  const privatePem = privateKey.export({ type: "pkcs8", format: "pem" }) as string;
  const publicPem = publicKey.export({ type: "spki", format: "pem" }) as string;
  const token = randomBytes(32).toString("hex");

  const data = {
    name: "テストクライアント",
    identifier: `test-client-${Date.now()}`,
    email: `client-${Date.now()}@example.com`,
    ...overrides,
  };

  const [result] = await pool.execute(
    `INSERT INTO clients (name, identifier, post_code, pref, city, address, tel, email, access_token, public_key, private_key, fingerprint, status)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
    [data.name, data.identifier, "100-0001", "東京都", "千代田区", "千代田1-1", "0312345678",
     data.email, token, publicPem, privatePem, "SHA256:test", 1],
  ) as mysql.ResultSetHeader[];

  return { id: result.insertId, name: data.name, identifier: data.identifier, token, privateKey: privatePem, publicKey: publicPem };
}

export async function makeInvitation(tokenStr?: string): Promise<TestInvitation> {
  const tok = tokenStr ?? randomBytes(16).toString("hex");
  const [result] = await pool.execute(
    "INSERT INTO invitations (token) VALUES (?)",
    [tok],
  ) as mysql.ResultSetHeader[];
  return { id: result.insertId, token: tok };
}

export async function makeNotification(staffId: number, title = "テスト通知"): Promise<TestNotification> {
  const [result] = await pool.execute(
    "INSERT INTO notifications (staff_id, title, body) VALUES (?, ?, ?)",
    [staffId, title, "通知本文"],
  ) as mysql.ResultSetHeader[];
  return { id: result.insertId, staffId, title };
}
