/**
 * Vitest グローバルセットアップ。
 * 環境変数は vitest.config.ts で .env.testing.local / .env.testing を読み込み済み。
 * テスト用テーブルの作成とテスト間のクリーンアップを担当します。
 */
import { beforeEach } from "vitest";
import * as mysql from "mysql2/promise";

// テスト用 DB 接続（テーブル作成・トランケート専用）
const adminPool = mysql.createPool({
  host: process.env.DB_HOST ?? "127.0.0.1",
  port: Number(process.env.DB_PORT ?? 3306),
  database: process.env.DB_DATABASE ?? "authorization_test",
  user: process.env.DB_USERNAME ?? "develop",
  password: process.env.DB_PASSWORD ?? "docker#DOCKER1234",
  charset: "utf8mb4",
  multipleStatements: true,
});

const DROP_TABLES_SQL = `
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS \`notifications\`;
DROP TABLE IF EXISTS \`invitations\`;
DROP TABLE IF EXISTS \`clients\`;
DROP TABLE IF EXISTS \`staffs\`;
SET FOREIGN_KEY_CHECKS=1;
`;

const CREATE_TABLES_SQL = `
CREATE TABLE \`staffs\` (
  \`id\`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`name\`          VARCHAR(255)    NOT NULL,
  \`email\`         VARCHAR(255)    NOT NULL,
  \`provider\`      INT             NOT NULL,
  \`provider_id\`   VARCHAR(255)    NOT NULL,
  \`avatar\`        VARCHAR(255),
  \`role\`          INT             NOT NULL DEFAULT 0,
  \`created_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP,
  \`updated_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  \`deleted_at\`    DATETIME        NULL,
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`staffs_email_unique\` (\`email\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE \`clients\` (
  \`id\`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`name\`          VARCHAR(255)    NOT NULL,
  \`identifier\`    VARCHAR(255)    NOT NULL,
  \`post_code\`     VARCHAR(255)    DEFAULT '',
  \`pref\`          VARCHAR(255)    DEFAULT '',
  \`city\`          VARCHAR(255)    DEFAULT '',
  \`address\`       VARCHAR(255)    DEFAULT '',
  \`building\`      VARCHAR(255)    DEFAULT '',
  \`tel\`           VARCHAR(255)    DEFAULT '',
  \`email\`         VARCHAR(255)    DEFAULT '',
  \`status\`        INT             DEFAULT 1,
  \`access_token\`  VARCHAR(512),
  \`public_key\`    TEXT,
  \`private_key\`   TEXT,
  \`fingerprint\`   VARCHAR(255),
  \`start_at\`      DATETIME        NULL,
  \`stop_at\`       DATETIME        NULL,
  \`created_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP,
  \`created_by\`    INT UNSIGNED    NOT NULL DEFAULT 0,
  \`updated_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  \`updated_by\`    INT UNSIGNED    NOT NULL DEFAULT 0,
  \`deleted_at\`    DATETIME        NULL,
  \`deleted_by\`    INT UNSIGNED    NULL,
  \`version\`       INT UNSIGNED    NOT NULL DEFAULT 1,
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`idx_clients_identifier\` (\`identifier\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE \`invitations\` (
  \`id\`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`token\`         VARCHAR(255)    NOT NULL,
  \`created_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP,
  \`updated_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (\`id\`),
  UNIQUE KEY \`invitations_token_unique\` (\`token\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE \`notifications\` (
  \`id\`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  \`staff_id\`      BIGINT UNSIGNED NOT NULL,
  \`message_type\`  INT UNSIGNED    NOT NULL DEFAULT 1,
  \`title\`         VARCHAR(255)    NOT NULL,
  \`message\`       VARCHAR(512)    NOT NULL DEFAULT '',
  \`url\`           VARCHAR(255)    NULL,
  \`read\`          BOOLEAN         NOT NULL DEFAULT FALSE,
  \`created_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP,
  \`created_by\`    INT UNSIGNED    NOT NULL DEFAULT 0,
  \`updated_at\`    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  \`updated_by\`    INT UNSIGNED    NOT NULL DEFAULT 0,
  \`version\`       INT UNSIGNED    NOT NULL DEFAULT 1,
  PRIMARY KEY (\`id\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
`;

// テスト開始時に DROP→CREATE でクリーンな状態を保証する（1回だけ実行）
let tablesCreated = false;

async function ensureTables() {
  if (tablesCreated) return;
  const conn = await adminPool.getConnection();
  try {
    for (const stmt of DROP_TABLES_SQL.split(";").map(s => s.trim()).filter(Boolean)) {
      await conn.query(stmt);
    }
    for (const stmt of CREATE_TABLES_SQL.split(";").map(s => s.trim()).filter(Boolean)) {
      await conn.query(stmt);
    }
    tablesCreated = true;
  } finally {
    conn.release();
  }
}

// 各テスト前にテーブルをクリア
beforeEach(async () => {
  await ensureTables();
  const conn = await adminPool.getConnection();
  try {
    await conn.query("SET FOREIGN_KEY_CHECKS=0");
    for (const table of ["notifications", "invitations", "clients", "staffs"]) {
      await conn.query(`TRUNCATE TABLE \`${table}\``);
    }
    await conn.query("SET FOREIGN_KEY_CHECKS=1");
  } finally {
    conn.release();
  }
});
