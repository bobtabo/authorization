//! スタッフリポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use chrono::NaiveDateTime;
use sqlx::{MySqlPool, QueryBuilder};
use crate::domain::staff::{
    condition::Condition,
    entity::Staff,
    repository::{DomainError, Repository},
};

#[derive(sqlx::FromRow)]
struct StaffRow {
    id:            u32,
    name:          String,
    email:         String,
    provider:      i32,
    provider_id:   String,
    avatar:        Option<String>,
    role:          i32,
    last_login_at: Option<NaiveDateTime>,
    created_at:    NaiveDateTime,
    created_by:    Option<u32>,
    updated_at:    NaiveDateTime,
    updated_by:    Option<u32>,
    deleted_at:    Option<NaiveDateTime>,
    deleted_by:    Option<u32>,
    version:       i32,
}

fn row_to_entity(r: StaffRow) -> Staff {
    Staff {
        id:            r.id,
        name:          r.name,
        email:         r.email,
        provider:      r.provider,
        provider_id:   r.provider_id,
        avatar:        r.avatar,
        role:          r.role,
        last_login_at: r.last_login_at,
        created_at:    r.created_at,
        created_by:    r.created_by,
        updated_at:    r.updated_at,
        updated_by:    r.updated_by,
        deleted_at:    r.deleted_at,
        deleted_by:    r.deleted_by,
        version:       r.version,
    }
}

/// SQLx を用いたスタッフリポジトリ実装。
pub struct SqlxStaffRepository {
    pool: MySqlPool,
}

impl SqlxStaffRepository {
    /// プールを受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    async fn fetch_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError> {
        let row = sqlx::query_as::<_, StaffRow>(
            "SELECT * FROM staffs WHERE id = ?"
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }
}

#[async_trait]
impl Repository for SqlxStaffRepository {
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Staff>, DomainError> {
        let mut qb: QueryBuilder<sqlx::MySql> = QueryBuilder::new(
            "SELECT * FROM staffs WHERE 1=1"
        );
        if let Some(ref kw) = cond.keyword {
            if !kw.is_empty() {
                let like = format!("%{}%", kw);
                qb.push(" AND (name LIKE ");
                qb.push_bind(like.clone());
                qb.push(" OR email LIKE ");
                qb.push_bind(like);
                qb.push(")");
            }
        }
        if !cond.roles.is_empty() {
            qb.push(" AND role IN (");
            let mut sep = qb.separated(", ");
            for r in &cond.roles {
                sep.push_bind(*r);
            }
            qb.push(")");
        }
        qb.push(" ORDER BY id ASC");
        let rows = qb.build_query_as::<StaffRow>().fetch_all(&self.pool).await?;
        Ok(rows.into_iter().map(row_to_entity).collect())
    }

    async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError> {
        let row = sqlx::query_as::<_, StaffRow>(
            "SELECT * FROM staffs WHERE id = ? AND deleted_at IS NULL LIMIT 1"
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }

    async fn find_by_provider(&self, provider: i32, provider_id: &str) -> Result<Option<Staff>, DomainError> {
        let row = sqlx::query_as::<_, StaffRow>(
            "SELECT * FROM staffs WHERE provider = ? AND provider_id = ? LIMIT 1"
        )
        .bind(provider)
        .bind(provider_id)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }

    async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError> {
        let rows = sqlx::query_as::<_, StaffRow>(
            "SELECT * FROM staffs WHERE deleted_at IS NULL ORDER BY id ASC"
        )
        .fetch_all(&self.pool)
        .await?;
        Ok(rows.into_iter().map(row_to_entity).collect())
    }

    async fn save(&self, s: Staff) -> Result<Staff, DomainError> {
        if s.id == 0 {
            let result = sqlx::query(
                "INSERT INTO staffs (name, email, provider, provider_id, avatar, role, \
                 last_login_at, created_at, created_by, updated_at, updated_by, \
                 deleted_at, deleted_by, version) \
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )
            .bind(&s.name)
            .bind(&s.email)
            .bind(s.provider)
            .bind(&s.provider_id)
            .bind(&s.avatar)
            .bind(s.role)
            .bind(s.last_login_at)
            .bind(s.created_at)
            .bind(s.created_by)
            .bind(s.updated_at)
            .bind(s.updated_by)
            .bind(s.deleted_at)
            .bind(s.deleted_by)
            .bind(s.version)
            .execute(&self.pool)
            .await?;
            let new_id = result.last_insert_id() as u32;
            Ok(self.fetch_by_id(new_id).await?.unwrap())
        } else {
            sqlx::query(
                "UPDATE staffs SET name = ?, email = ?, provider = ?, provider_id = ?, avatar = ?, \
                 role = ?, last_login_at = ?, updated_at = ?, updated_by = ?, \
                 deleted_at = ?, deleted_by = ?, version = version + 1 \
                 WHERE id = ?"
            )
            .bind(&s.name)
            .bind(&s.email)
            .bind(s.provider)
            .bind(&s.provider_id)
            .bind(&s.avatar)
            .bind(s.role)
            .bind(s.last_login_at)
            .bind(s.updated_at)
            .bind(s.updated_by)
            .bind(s.deleted_at)
            .bind(s.deleted_by)
            .bind(s.id)
            .execute(&self.pool)
            .await?;
            Ok(self.fetch_by_id(s.id).await?.unwrap())
        }
    }

    async fn update_role(&self, id: u32, role: i32, updated_by: u32) -> Result<bool, DomainError> {
        let now = chrono::Local::now().naive_local();
        let result = sqlx::query(
            "UPDATE staffs SET role = ?, updated_at = ?, updated_by = ?, version = version + 1 \
             WHERE id = ? AND deleted_at IS NULL"
        )
        .bind(role)
        .bind(now)
        .bind(updated_by)
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(result.rows_affected() > 0)
    }

    async fn soft_delete(&self, id: u32, deleted_by: u32) -> Result<bool, DomainError> {
        let now = chrono::Local::now().naive_local();
        let result = sqlx::query(
            "UPDATE staffs SET deleted_at = ?, deleted_by = ? \
             WHERE id = ? AND deleted_at IS NULL"
        )
        .bind(now)
        .bind(deleted_by)
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(result.rows_affected() > 0)
    }

    async fn restore(&self, id: u32) -> Result<bool, DomainError> {
        let result = sqlx::query(
            "UPDATE staffs SET deleted_at = NULL, deleted_by = NULL \
             WHERE id = ? AND deleted_at IS NOT NULL"
        )
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(result.rows_affected() > 0)
    }
}
