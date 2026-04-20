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

#[derive(Deserialize)]
pub struct IndexQuery {
    pub cursor: Option<String>,
    pub limit:  Option<i64>,
}

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
            let items: Vec<Value> = page.items.iter()
                .map(|n| crate::usecase::notification::Interactor::map_notification(n))
                .collect();
            (StatusCode::OK, Json(json!({"items": items, "next_cursor": page.next_cursor})))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn read_all(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }
    match state.notification_uc.bulk_mark_read(staff_id).await {
        Ok(updated) => (StatusCode::OK, Json(json!({"updated": updated}))),
        Err(_)      => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn read(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> (StatusCode, Json<Value>) {
    match state.notification_uc.mark_read(id).await {
        Ok(_)  => (StatusCode::OK, Json(json!({"id": id}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}
