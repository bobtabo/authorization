//! クライアントリポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use chrono::NaiveDateTime;
use sqlx::{MySqlPool, QueryBuilder};
use crate::domain::client::{
    condition::Condition,
    entity::Client,
    repository::{DomainError, Repository},
};

#[derive(sqlx::FromRow)]
struct ClientRow {
    id:           u64,
    name:         String,
    identifier:   String,
    post_code:    String,
    pref:         String,
    city:         String,
    address:      String,
    building:     String,
    tel:          String,
    email:        String,
    access_token: String,
    private_key:  String,
    public_key:   String,
    fingerprint:  String,
    status:       i32,
    start_at:     Option<NaiveDateTime>,
    stop_at:      Option<NaiveDateTime>,
    created_at:   NaiveDateTime,
    created_by:   Option<u32>,
    updated_at:   NaiveDateTime,
    updated_by:   Option<u32>,
    deleted_at:   Option<NaiveDateTime>,
    deleted_by:   Option<u32>,
    version:      i32,
}

fn row_to_entity(r: ClientRow) -> Client {
    Client {
        id:           r.id,
        name:         r.name,
        identifier:   r.identifier,
        post_code:    r.post_code,
        pref:         r.pref,
        city:         r.city,
        address:      r.address,
        building:     r.building,
        tel:          r.tel,
        email:        r.email,
        access_token: r.access_token,
        private_key:  r.private_key,
        public_key:   r.public_key,
        fingerprint:  r.fingerprint,
        status:       r.status,
        start_at:     r.start_at,
        stop_at:      r.stop_at,
        created_at:   r.created_at,
        created_by:   r.created_by,
        updated_at:   r.updated_at,
        updated_by:   r.updated_by,
        deleted_at:   r.deleted_at,
        deleted_by:   r.deleted_by,
        version:      r.version,
    }
}

/// SQLx を用いたクライアントリポジトリ実装。
pub struct SqlxClientRepository {
    pool: MySqlPool,
}

impl SqlxClientRepository {
    /// プールを受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }

    async fn fetch_by_id(&self, id: u64) -> Result<Option<Client>, DomainError> {
        let row = sqlx::query_as::<_, ClientRow>(
            "SELECT * FROM clients WHERE id = ?"
        )
        .bind(id)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }
}

#[async_trait]
impl Repository for SqlxClientRepository {
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Client>, DomainError> {
        let mut qb: QueryBuilder<sqlx::MySql> = QueryBuilder::new(
            "SELECT * FROM clients WHERE 1=1"
        );
        if let Some(ref kw) = cond.keyword {
            if !kw.is_empty() {
                qb.push(" AND name LIKE ");
                qb.push_bind(format!("%{}%", kw));
            }
        }
        if let Some(sf) = cond.start_from {
            qb.push(" AND start_at >= ");
            qb.push_bind(sf);
        }
        if let Some(st) = cond.start_to {
            qb.push(" AND start_at <= ");
            qb.push_bind(st);
        }
        if !cond.statuses.is_empty() {
            qb.push(" AND status IN (");
            let mut sep = qb.separated(", ");
            for s in &cond.statuses {
                sep.push_bind(*s);
            }
            qb.push(")");
        }
        qb.push(" ORDER BY id ASC");
        let rows = qb.build_query_as::<ClientRow>().fetch_all(&self.pool).await?;
        Ok(rows.into_iter().map(row_to_entity).collect())
    }

    async fn find_by_id(&self, id: u64) -> Result<Option<Client>, DomainError> {
        self.fetch_by_id(id).await
    }

    async fn find_by_access_token(&self, token: &str) -> Result<Option<Client>, DomainError> {
        let row = sqlx::query_as::<_, ClientRow>(
            "SELECT * FROM clients WHERE access_token = ? AND status = 2 AND deleted_at IS NULL LIMIT 1"
        )
        .bind(token)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }

    async fn find_by_identifier(&self, identifier: &str) -> Result<Option<Client>, DomainError> {
        let row = sqlx::query_as::<_, ClientRow>(
            "SELECT * FROM clients WHERE identifier = ? AND deleted_at IS NULL LIMIT 1"
        )
        .bind(identifier)
        .fetch_optional(&self.pool)
        .await?;
        Ok(row.map(row_to_entity))
    }

    async fn save(&self, c: Client) -> Result<Client, DomainError> {
        if c.id == 0 {
            let result = sqlx::query(
                "INSERT INTO clients (name, identifier, post_code, pref, city, address, building, tel, email, \
                 access_token, private_key, public_key, fingerprint, status, start_at, stop_at, \
                 created_at, created_by, updated_at, updated_by, deleted_at, deleted_by, version) \
                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )
            .bind(&c.name)
            .bind(&c.identifier)
            .bind(&c.post_code)
            .bind(&c.pref)
            .bind(&c.city)
            .bind(&c.address)
            .bind(&c.building)
            .bind(&c.tel)
            .bind(&c.email)
            .bind(&c.access_token)
            .bind(&c.private_key)
            .bind(&c.public_key)
            .bind(&c.fingerprint)
            .bind(c.status)
            .bind(c.start_at)
            .bind(c.stop_at)
            .bind(c.created_at)
            .bind(c.created_by)
            .bind(c.updated_at)
            .bind(c.updated_by)
            .bind(c.deleted_at)
            .bind(c.deleted_by)
            .bind(c.version)
            .execute(&self.pool)
            .await?;
            let new_id = result.last_insert_id();
            Ok(self.fetch_by_id(new_id).await?.unwrap())
        } else {
            sqlx::query(
                "UPDATE clients SET name = ?, identifier = ?, post_code = ?, pref = ?, city = ?, \
                 address = ?, building = ?, tel = ?, email = ?, access_token = ?, private_key = ?, \
                 public_key = ?, fingerprint = ?, status = ?, start_at = ?, stop_at = ?, \
                 updated_at = ?, updated_by = ?, deleted_at = ?, deleted_by = ?, version = version + 1 \
                 WHERE id = ?"
            )
            .bind(&c.name)
            .bind(&c.identifier)
            .bind(&c.post_code)
            .bind(&c.pref)
            .bind(&c.city)
            .bind(&c.address)
            .bind(&c.building)
            .bind(&c.tel)
            .bind(&c.email)
            .bind(&c.access_token)
            .bind(&c.private_key)
            .bind(&c.public_key)
            .bind(&c.fingerprint)
            .bind(c.status)
            .bind(c.start_at)
            .bind(c.stop_at)
            .bind(c.updated_at)
            .bind(c.updated_by)
            .bind(c.deleted_at)
            .bind(c.deleted_by)
            .bind(c.id)
            .execute(&self.pool)
            .await?;
            Ok(self.fetch_by_id(c.id).await?.unwrap())
        }
    }

    async fn soft_delete(&self, id: u64, deleted_by: u32) -> Result<(), DomainError> {
        let now = chrono::Local::now().naive_local();
        sqlx::query(
            "UPDATE clients SET deleted_at = ?, deleted_by = ? WHERE id = ?"
        )
        .bind(now)
        .bind(deleted_by)
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(())
    }
}
