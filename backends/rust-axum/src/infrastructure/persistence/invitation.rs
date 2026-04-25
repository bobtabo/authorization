//! 招待リポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use chrono::NaiveDateTime;
use sqlx::MySqlPool;
use crate::domain::invitation::{
    value_objects::Vo,
    repository::{DomainError, Repository},
};

#[derive(sqlx::FromRow)]
struct InvitationRow {
    #[allow(dead_code)]
    id:         u32,
    token:      String,
    #[allow(dead_code)]
    created_at: NaiveDateTime,
    #[allow(dead_code)]
    created_by: Option<u32>,
    #[allow(dead_code)]
    updated_at: NaiveDateTime,
    #[allow(dead_code)]
    updated_by: Option<u32>,
    #[allow(dead_code)]
    deleted_at: Option<NaiveDateTime>,
    #[allow(dead_code)]
    deleted_by: Option<u32>,
    #[allow(dead_code)]
    version:    i32,
}

/// SQLx を用いた招待リポジトリ実装。
pub struct SqlxInvitationRepository {
    pool:         MySqlPool,
    frontend_url: String,
}

impl SqlxInvitationRepository {
    /// プールとフロントエンド URL を受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool, frontend_url: String) -> Self {
        Self { pool, frontend_url }
    }

    fn build_vo(&self, token: &str) -> Vo {
        let url = format!("{}/register?token={}", self.frontend_url, token);
        let display_url = if url.len() > 50 {
            format!("{}...{}", &url[..20], &url[url.len() - 20..])
        } else {
            url.clone()
        };
        Vo { token: token.to_string(), url, display_url }
    }
}

#[async_trait]
impl Repository for SqlxInvitationRepository {
    async fn get_current(&self) -> Result<Option<Vo>, DomainError> {
        let row = sqlx::query_as::<_, InvitationRow>(
            "SELECT * FROM invitations ORDER BY id DESC LIMIT 1"
        )
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(|r| self.build_vo(&r.token)))
    }

    async fn issue(&self) -> Result<Vo, DomainError> {
        let token = generate_token();
        let now = chrono::Local::now().naive_local();
        sqlx::query(
            "INSERT INTO invitations (token, created_at, updated_at, version) VALUES (?, ?, ?, 0)"
        )
        .bind(&token)
        .bind(now)
        .bind(now)
        .execute(&self.pool)
        .await?;
        Ok(self.build_vo(&token))
    }

    async fn find_by_token(&self, token: &str) -> Result<Option<Vo>, DomainError> {
        let row = sqlx::query_as::<_, InvitationRow>(
            "SELECT * FROM invitations WHERE token = ? AND deleted_at IS NULL LIMIT 1"
        )
        .bind(token)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(|r| self.build_vo(&r.token)))
    }
}

fn generate_token() -> String {
    let mut bytes = [0u8; 16];
    rand::RngCore::fill_bytes(&mut rand::rngs::OsRng, &mut bytes);
    hex::encode(bytes)
}
