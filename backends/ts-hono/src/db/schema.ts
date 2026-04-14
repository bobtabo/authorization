import {
  mysqlTable, bigint, varchar, text, int, boolean, datetime,
} from "drizzle-orm/mysql-core";
import { sql } from "drizzle-orm";

export const clients = mysqlTable("clients", {
  id: bigint("id", { mode: "number", unsigned: true }).primaryKey().autoincrement(),
  name: varchar("name", { length: 255 }).notNull(),
  identifier: varchar("identifier", { length: 255 }).notNull(),
  postCode: varchar("post_code", { length: 255 }).default(""),
  pref: varchar("pref", { length: 255 }).default(""),
  city: varchar("city", { length: 255 }).default(""),
  address: varchar("address", { length: 255 }).default(""),
  building: varchar("building", { length: 255 }).default(""),
  tel: varchar("tel", { length: 255 }).default(""),
  email: varchar("email", { length: 255 }).default(""),
  status: int("status").default(0),
  token: varchar("access_token", { length: 512 }),
  publicKey: text("public_key"),
  privateKey: text("private_key"),
  fingerprint: varchar("fingerprint", { length: 255 }),
  startedAt: datetime("start_at"),
  stoppedAt: datetime("stop_at"),
  createdAt: datetime("created_at").default(sql`CURRENT_TIMESTAMP`),
  updatedAt: datetime("updated_at").default(sql`CURRENT_TIMESTAMP`),
  deletedAt: datetime("deleted_at"),
});

export const staffs = mysqlTable("staffs", {
  id: bigint("id", { mode: "number", unsigned: true }).primaryKey().autoincrement(),
  name: varchar("name", { length: 255 }).notNull(),
  email: varchar("email", { length: 255 }).notNull(),
  provider: int("provider").notNull(),
  providerId: varchar("provider_id", { length: 255 }).notNull(),
  avatar: varchar("avatar", { length: 255 }),
  role: int("role").default(0),
  createdAt: datetime("created_at").default(sql`CURRENT_TIMESTAMP`),
  updatedAt: datetime("updated_at").default(sql`CURRENT_TIMESTAMP`),
  deletedAt: datetime("deleted_at"),
});

export const invitations = mysqlTable("invitations", {
  id: bigint("id", { mode: "number", unsigned: true }).primaryKey().autoincrement(),
  token: varchar("token", { length: 255 }).notNull(),
  createdAt: datetime("created_at").default(sql`CURRENT_TIMESTAMP`),
  updatedAt: datetime("updated_at").default(sql`CURRENT_TIMESTAMP`),
});

export const notifications = mysqlTable("notifications", {
  id: bigint("id", { mode: "number", unsigned: true }).primaryKey().autoincrement(),
  staffId: bigint("staff_id", { mode: "number", unsigned: true }).notNull(),
  messageType: int("message_type").notNull().default(1),
  title: varchar("title", { length: 255 }).notNull(),
  message: varchar("message", { length: 512 }).notNull().default(""),
  url: varchar("url", { length: 255 }),
  read: boolean("read").default(false),
  createdAt: datetime("created_at").default(sql`CURRENT_TIMESTAMP`),
  createdBy: int("created_by").notNull().default(0),
  updatedAt: datetime("updated_at").default(sql`CURRENT_TIMESTAMP`),
  updatedBy: int("updated_by").notNull().default(0),
  version: int("version").notNull().default(0),
});

export type Client = typeof clients.$inferSelect;
export type Staff = typeof staffs.$inferSelect;
export type Invitation = typeof invitations.$inferSelect;
export type Notification = typeof notifications.$inferSelect;
