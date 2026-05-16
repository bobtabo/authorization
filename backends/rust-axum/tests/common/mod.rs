use axum::Router;
use sqlx::MySqlPool;
use std::sync::Arc;
use tokio::sync::OnceCell;
use authorization::{build_router, build_state, config::Config};

static SCHEMA_INIT: OnceCell<()> = OnceCell::const_new();

pub async fn build_test_app() -> (Router, MySqlPool) {
    let cfg = Arc::new(Config::load());
    let (state, pool) = build_state(cfg).await;
    let pool_for_init = pool.clone();
    SCHEMA_INIT.get_or_init(|| async move {
        ensure_schema(&pool_for_init).await;
    }).await;
    let app = build_router(state);
    (app, pool)
}

async fn ensure_schema(pool: &MySqlPool) {
    // Drop and recreate tables to ensure correct column types for SQLx 0.8
    // (DATETIME instead of TIMESTAMP, since NaiveDateTime maps to DATETIME not TIMESTAMP)
    sqlx::query("SET FOREIGN_KEY_CHECKS=0").execute(pool).await.unwrap();
    for t in &["notifications", "invitations", "clients", "staffs"] {
        sqlx::query(&format!("DROP TABLE IF EXISTS `{}`", t)).execute(pool).await.unwrap();
    }
    sqlx::query("SET FOREIGN_KEY_CHECKS=1").execute(pool).await.unwrap();

    sqlx::query(
        "CREATE TABLE `staffs` (
            `id`            INT UNSIGNED    NOT NULL AUTO_INCREMENT,
            `name`          VARCHAR(100)    NOT NULL,
            `email`         VARCHAR(255)    NOT NULL,
            `provider`      INT             NOT NULL,
            `provider_id`   VARCHAR(255)    NOT NULL,
            `avatar`        TEXT,
            `role`          INT UNSIGNED    NOT NULL DEFAULT 2,
            `last_login_at` DATETIME        NULL     DEFAULT NULL,
            `created_at`    DATETIME        NOT NULL,
            `created_by`    INT UNSIGNED,
            `updated_at`    DATETIME        NOT NULL,
            `updated_by`    INT UNSIGNED,
            `deleted_at`    DATETIME        NULL,
            `deleted_by`    INT UNSIGNED    NULL,
            `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
            PRIMARY KEY (`id`),
            UNIQUE KEY `staffs_email_unique` (`email`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
    ).execute(pool).await.unwrap();

    sqlx::query(
        "CREATE TABLE `clients` (
            `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
            `name`          VARCHAR(255)    NOT NULL,
            `identifier`    VARCHAR(255)    NOT NULL,
            `post_code`     VARCHAR(8),
            `pref`          VARCHAR(50),
            `city`          VARCHAR(100),
            `address`       VARCHAR(255),
            `building`      VARCHAR(255),
            `tel`           VARCHAR(255),
            `email`         VARCHAR(255),
            `access_token`  VARCHAR(512),
            `private_key`   TEXT,
            `public_key`    TEXT,
            `fingerprint`   VARCHAR(255),
            `status`        INT UNSIGNED    NOT NULL DEFAULT 1,
            `start_at`      DATETIME        NULL,
            `stop_at`       DATETIME        NULL,
            `created_at`    DATETIME        NOT NULL,
            `created_by`    INT UNSIGNED,
            `updated_at`    DATETIME        NOT NULL,
            `updated_by`    INT UNSIGNED,
            `deleted_at`    DATETIME        NULL,
            `deleted_by`    INT UNSIGNED    NULL,
            `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
            PRIMARY KEY (`id`),
            UNIQUE KEY `idx_clients_identifier` (`identifier`),
            UNIQUE KEY `idx_clients_access_token` (`access_token`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
    ).execute(pool).await.unwrap();

    sqlx::query(
        "CREATE TABLE `invitations` (
            `id`            INT UNSIGNED    NOT NULL AUTO_INCREMENT,
            `token`         VARCHAR(255)    NOT NULL,
            `role`          TINYINT UNSIGNED NOT NULL DEFAULT 2,
            `created_at`    DATETIME        NOT NULL,
            `created_by`    INT UNSIGNED,
            `updated_at`    DATETIME        NOT NULL,
            `updated_by`    INT UNSIGNED,
            `deleted_at`    DATETIME        NULL,
            `deleted_by`    INT UNSIGNED    NULL,
            `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
            PRIMARY KEY (`id`),
            UNIQUE KEY `invitations_token_unique` (`token`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
    ).execute(pool).await.unwrap();

    sqlx::query(
        "CREATE TABLE `notifications` (
            `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
            `staff_id`      INT UNSIGNED    NOT NULL,
            `message_type`  INT UNSIGNED    NOT NULL DEFAULT 1,
            `title`         VARCHAR(255)    NOT NULL,
            `message`       VARCHAR(512)    NOT NULL DEFAULT '',
            `url`           VARCHAR(255)    NULL,
            `read`          TINYINT(1)      NOT NULL DEFAULT 0,
            `created_at`    DATETIME        NOT NULL,
            `created_by`    INT UNSIGNED,
            `updated_at`    DATETIME        NOT NULL,
            `updated_by`    INT UNSIGNED,
            `deleted_at`    DATETIME        NULL,
            `deleted_by`    INT UNSIGNED    NULL,
            `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
            PRIMARY KEY (`id`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
    ).execute(pool).await.unwrap();
}

pub async fn truncate_tables(pool: &MySqlPool) {
    sqlx::query("SET FOREIGN_KEY_CHECKS=0").execute(pool).await.unwrap();
    for table in &["notifications", "invitations", "clients", "staffs"] {
        sqlx::query(&format!("TRUNCATE TABLE {}", table)).execute(pool).await.unwrap();
    }
    sqlx::query("SET FOREIGN_KEY_CHECKS=1").execute(pool).await.unwrap();
}

pub async fn create_staff(pool: &MySqlPool) -> u32 {
    let email = format!("staff-{}@example.com", uuid::Uuid::new_v4().simple());
    let now = chrono::Local::now().naive_local();
    let result = sqlx::query(
        "INSERT INTO staffs (name, email, provider, provider_id, role, \
         created_at, created_by, updated_at, updated_by, version) \
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    .bind("テストスタッフ")
    .bind(&email)
    .bind(1i32)
    .bind(format!("test-{}", uuid::Uuid::new_v4().simple()))
    .bind(1i32)
    .bind(now)
    .bind(0u32)
    .bind(now)
    .bind(0u32)
    .bind(1i32)
    .execute(pool)
    .await
    .unwrap();
    result.last_insert_id() as u32
}

pub struct ClientData {
    pub id:           u64,
    pub identifier:   String,
    pub access_token: String,
}

pub async fn create_client(pool: &MySqlPool) -> ClientData {
    use rsa::{RsaPrivateKey, pkcs1::{EncodeRsaPrivateKey, EncodeRsaPublicKey, LineEnding}};

    let mut rng = rand::rngs::OsRng;
    let private_key = RsaPrivateKey::new(&mut rng, 2048).unwrap();
    let public_key  = private_key.to_public_key();

    let priv_pem: String = private_key.to_pkcs1_pem(LineEnding::LF).unwrap().to_string();
    let pub_pem:  String = public_key.to_pkcs1_pem(LineEnding::LF).unwrap();

    let token      = hex::encode(rand::random::<[u8; 32]>());
    let id_str     = uuid::Uuid::new_v4().simple().to_string();
    let identifier = format!("test-client-{}", &id_str[..8]);
    let now        = chrono::Local::now().naive_local();

    let result = sqlx::query(
        "INSERT INTO clients (name, identifier, post_code, pref, city, address, building, tel, email, \
         access_token, private_key, public_key, fingerprint, status, \
         created_at, created_by, updated_at, updated_by, version) \
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    .bind("テストクライアント")
    .bind(&identifier)
    .bind("100-0001")
    .bind("東京都")
    .bind("千代田区")
    .bind("千代田1-1")
    .bind("")
    .bind("0312345678")
    .bind(format!("client-{}@example.com", uuid::Uuid::new_v4().simple()))
    .bind(&token)
    .bind(&priv_pem)
    .bind(&pub_pem)
    .bind("SHA256:test")
    .bind(2i32)  // ACTIVE
    .bind(now)
    .bind(0u32)
    .bind(now)
    .bind(0u32)
    .bind(1i32)
    .execute(pool)
    .await
    .unwrap();

    ClientData {
        id:           result.last_insert_id(),
        identifier,
        access_token: token,
    }
}

pub async fn create_invitation(pool: &MySqlPool, role: u8) -> String {
    let token = hex::encode(rand::random::<[u8; 16]>());
    let now = chrono::Local::now().naive_local();
    sqlx::query("INSERT INTO invitations (token, role, created_at, updated_at) VALUES (?, ?, ?, ?)")
        .bind(&token)
        .bind(role)
        .bind(now)
        .bind(now)
        .execute(pool)
        .await
        .unwrap();
    token
}

pub async fn create_notification(pool: &MySqlPool, staff_id: u32, title: &str, read: bool) -> u64 {
    let now = chrono::Local::now().naive_local();
    let result = sqlx::query(
        "INSERT INTO notifications (staff_id, message_type, title, message, `read`, \
         created_at, created_by, updated_at, updated_by, version) \
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
    )
    .bind(staff_id)
    .bind(1i32)
    .bind(title)
    .bind("テスト通知本文")
    .bind(read)
    .bind(now)
    .bind(0u32)
    .bind(now)
    .bind(0u32)
    .bind(1i32)
    .execute(pool)
    .await
    .unwrap();
    result.last_insert_id()
}
