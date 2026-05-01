//! 通知ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{
    extract::{Path, Query, State},
    http::StatusCode,
    Json,
};
use axum_extra::extract::CookieJar;
use serde::Deserialize;
use serde_json::{json, Value};

use crate::state::AppState;
use super::staff_id_from_cookie;

/// 通知一覧取得クエリ。
#[derive(Deserialize)]
pub struct IndexQuery {
    pub cursor: Option<String>,
    pub limit:  Option<i64>,
}

/// スタッフの未読・全件数を返します。
pub async fn counts(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }
    match state.notification_uc.counts(staff_id).await {
        Ok((unread, total)) => (StatusCode::OK, Json(json!({"unread": unread, "total": total}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

/// 通知一覧をカーソルページングで返します。
pub async fn index(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(q): Query<IndexQuery>,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }
    let limit = q.limit.unwrap_or(state.cfg.app.notification_default_limit);
    match state.notification_uc.list_page(staff_id, q.cursor, limit).await {
        Ok(page) => {
            let items: Vec<Value> = page.items.iter().map(|n| json!({
                "id":           n.id,
                "staff_id":     n.staff_id,
                "message_type": n.message_type,
                "title":        n.title,
                "message":      n.message,
                "url":          n.url,
                "read":         n.read,
                "created_at":   n.created_at,
                "updated_at":   n.updated_at,
            })).collect();
            (StatusCode::OK, Json(json!({"items": items, "next_cursor": page.next_cursor})))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

/// スタッフの全通知を一括既読にします。トランザクション内で処理します。
pub async fn read_all(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let updated = match state.notification_uc.bulk_mark_read(staff_id).await {
        Ok(n) => n,
        Err(_) => {
            let _ = tx.rollback().await;
            return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
        }
    };

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"updated": updated})))
}

/// 通知を既読にします。トランザクション内で処理します。
pub async fn read(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> (StatusCode, Json<Value>) {
    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(_) = state.notification_uc.mark_read(id).await {
        let _ = tx.rollback().await;
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"id": id})))
}
