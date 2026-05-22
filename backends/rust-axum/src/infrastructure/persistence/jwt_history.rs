//! JWT 履歴リポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use chrono::{DateTime, Utc};
use sqlx::MySqlPool;
use crate::domain::client::{
    entity::JwtHistory,
    repository::{DomainError, JwtHistoryRepository},
};

#[derive(sqlx::FromRow)]
struct JwtHistoryRow {
    id:         u64,
    client_id:  u64,
    member_id:  String,
    issue_at:   DateTime<Utc>,
    jwt:        String,
    created_at: DateTime<Utc>,
    deleted_at: Option<DateTime<Utc>>,
}

fn row_to_entity(r: JwtHistoryRow) -> JwtHistory {
    JwtHistory {
        id:         r.id,
        client_id:  r.client_id,
        member_id:  r.member_id,
        issue_at:   r.issue_at,
        jwt:        r.jwt,
        created_at: r.created_at,
        deleted_at: r.deleted_at,
    }
}

/// SQLx を用いた JWT 履歴リポジトリ実装。
pub struct SqlxJwtHistoryRepository {
    pool: MySqlPool,
}

impl SqlxJwtHistoryRepository {
    /// プールを受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl JwtHistoryRepository for SqlxJwtHistoryRepository {
    async fn find_by_client_id(&self, client_id: u64) -> Result<Vec<JwtHistory>, DomainError> {
        let rows = sqlx::query_as::<_, JwtHistoryRow>(
            "SELECT id, client_id, member_id, issue_at, jwt, created_at, deleted_at \
             FROM jwt_histories \
             WHERE client_id = ? AND deleted_at IS NULL \
             ORDER BY issue_at DESC"
        )
        .bind(client_id)
        .fetch_all(&self.pool)
        .await?;
        Ok(rows.into_iter().map(row_to_entity).collect())
    }

    async fn save(
        &self,
        client_id: u64,
        member_id: &str,
        issue_at: DateTime<Utc>,
        jwt: &str,
    ) -> Result<(), DomainError> {
        let now = Utc::now();
        sqlx::query(
            "INSERT INTO jwt_histories \
             (client_id, member_id, issue_at, jwt, created_at, created_by, updated_at, updated_by, version) \
             VALUES (?, ?, ?, ?, ?, 0, ?, 0, 1)"
        )
        .bind(client_id)
        .bind(member_id)
        .bind(issue_at)
        .bind(jwt)
        .bind(now)
        .bind(now)
        .execute(&self.pool)
        .await?;
        Ok(())
    }
}
