//! 通知リポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use base64::Engine;
use chrono::{DateTime, Utc};
use sqlx::{MySqlPool, QueryBuilder};
use crate::domain::notification::{
    entity::Notification,
    value_objects::EntityPage,
    repository::{DomainError, Repository},
};

#[derive(sqlx::FromRow)]
struct NotificationRow {
    id:           u64,
    staff_id:     u32,
    message_type: u32,
    title:        String,
    message:      String,
    url:          Option<String>,
    read:         bool,
    created_at: DateTime<Utc>,
    created_by:   Option<u32>,
    updated_at: DateTime<Utc>,
    updated_by:   Option<u32>,
    deleted_at: Option<DateTime<Utc>>,
    deleted_by:   Option<u32>,
    version:      u32,
}

fn row_to_entity(r: NotificationRow) -> Notification {
    Notification {
        id:           r.id,
        staff_id:     r.staff_id,
        message_type: r.message_type as i32,
        title:        r.title,
        message:      r.message,
        url:          r.url,
        read:         r.read,
        created_at:   r.created_at,
        created_by:   r.created_by,
        updated_at:   r.updated_at,
        updated_by:   r.updated_by,
        deleted_at:   r.deleted_at,
        deleted_by:   r.deleted_by,
        version:      r.version as i32,
    }
}

fn encode_cursor(unix_sec: i64, id: i64) -> String {
    let raw = format!("{},{}", unix_sec, id);
    base64::engine::general_purpose::STANDARD.encode(raw.as_bytes())
}

fn decode_cursor(cursor: &str) -> Option<(i64, i64)> {
    let bytes = base64::engine::general_purpose::STANDARD.decode(cursor).ok()?;
    let s = String::from_utf8(bytes).ok()?;
    let mut parts = s.splitn(2, ',');
    let unix_sec: i64 = parts.next()?.parse().ok()?;
    let id: i64 = parts.next()?.parse().ok()?;
    Some((unix_sec, id))
}

/// SQLx を用いた通知リポジトリ実装。
pub struct SqlxNotificationRepository {
    pool: MySqlPool,
}

impl SqlxNotificationRepository {
    /// プールを受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository for SqlxNotificationRepository {
    async fn list_page(&self, staff_id: u32, cursor: Option<&str>, limit: i32) -> Result<EntityPage, DomainError> {
        let fetch_limit = limit + 1;
        let mut qb: QueryBuilder<sqlx::MySql> = QueryBuilder::new(
            "SELECT * FROM notifications WHERE staff_id = "
        );
        qb.push_bind(staff_id);
        qb.push(" AND deleted_at IS NULL");

        if let Some(c) = cursor {
            if let Some((ts, cid)) = decode_cursor(c) {
                let ts_dt = chrono::DateTime::from_timestamp(ts, 0)
                    ;
                if let Some(dt) = ts_dt {
                    qb.push(" AND (created_at < ");
                    qb.push_bind(dt);
                    qb.push(" OR (created_at = ");
                    qb.push_bind(dt);
                    qb.push(" AND id < ");
                    qb.push_bind(cid as u64);
                    qb.push("))");
                }
            }
        }

        qb.push(" ORDER BY created_at DESC, id DESC LIMIT ");
        qb.push_bind(fetch_limit);

        let mut rows = qb.build_query_as::<NotificationRow>().fetch_all(&self.pool).await?;

        let next_cursor = if rows.len() as i32 > limit {
            rows.truncate(limit as usize);
            let last = &rows[rows.len() - 1];
            Some(encode_cursor(last.created_at.timestamp(), last.id as i64))
        } else {
            None
        };

        Ok(EntityPage {
            items:       rows.into_iter().map(row_to_entity).collect(),
            next_cursor,
        })
    }

    async fn counts(&self, staff_id: u32) -> Result<(i64, i64), DomainError> {
        let (total,): (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM notifications WHERE staff_id = ? AND deleted_at IS NULL"
        )
        .bind(staff_id)
        .fetch_one(&self.pool)
        .await?;

        let (unread,): (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM notifications WHERE staff_id = ? AND `read` = false AND deleted_at IS NULL"
        )
        .bind(staff_id)
        .fetch_one(&self.pool)
        .await?;

        Ok((unread, total))
    }

    async fn bulk_mark_read(&self, staff_id: i64, ids: Vec<i64>, all: bool) -> Result<i64, DomainError> {
        let now = chrono::Utc::now();
        let mut qb: QueryBuilder<sqlx::MySql> = QueryBuilder::new(
            "UPDATE notifications SET `read` = true, updated_at = "
        );
        qb.push_bind(now);
        qb.push(" WHERE staff_id = ");
        qb.push_bind(staff_id);
        qb.push(" AND `read` = false AND deleted_at IS NULL");

        if !all && !ids.is_empty() {
            qb.push(" AND id IN (");
            let mut sep = qb.separated(", ");
            for id in &ids {
                sep.push_bind(*id);
            }
            qb.push(")");
        }

        let result = qb.build().execute(&self.pool).await?;
        Ok(result.rows_affected() as i64)
    }

    async fn store(&self, staff_id: u32, message_type: i32, title: &str, message: &str, created_by: u32, url: Option<&str>) -> Result<(), DomainError> {
        let now = chrono::Utc::now();
        sqlx::query(
            "INSERT INTO notifications (staff_id, message_type, title, message, url, `read`, \
             created_at, created_by, updated_at, updated_by, version) \
             VALUES (?, ?, ?, ?, ?, false, ?, ?, ?, ?, 1)"
        )
        .bind(staff_id)
        .bind(message_type)
        .bind(title)
        .bind(message)
        .bind(url)
        .bind(now)
        .bind(created_by)
        .bind(now)
        .bind(created_by)
        .execute(&self.pool)
        .await?;
        Ok(())
    }

    async fn patch(&self, id: i64, read: bool) -> Result<bool, DomainError> {
        let now = chrono::Utc::now();
        let result = sqlx::query(
            "UPDATE notifications SET `read` = ?, updated_at = ? WHERE id = ?"
        )
        .bind(read)
        .bind(now)
        .bind(id)
        .execute(&self.pool)
        .await?;
        Ok(result.rows_affected() > 0)
    }
}
