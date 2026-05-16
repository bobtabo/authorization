//! 招待管理ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{extract::{Query, State}, http::StatusCode, Json};
use axum_extra::extract::CookieJar;
use serde::Deserialize;
use serde_json::{json, Value};

use crate::{
    state::AppState,
    usecase::invitation::dto::RoleDto,
};
use super::staff_id_from_cookie;

#[derive(Deserialize)]
pub struct InvitationQuery {
    role: Option<u8>,
}

/// クエリパラメーターから role を解決します（1 or 2、それ以外は Err）。
fn resolve_role(query: &InvitationQuery) -> Result<u8, ()> {
    let role = query.role.unwrap_or(2);
    if role == 1 || role == 2 {
        Ok(role)
    } else {
        Err(())
    }
}

/// 現在の招待トークン情報を返します。
pub async fn index(
    State(state): State<AppState>,
    Query(query): Query<InvitationQuery>,
) -> (StatusCode, Json<Value>) {
    let role = match resolve_role(&query) {
        Ok(r)  => r,
        Err(()) => return (StatusCode::BAD_REQUEST, Json(json!({"error": "invalid_role"}))),
    };
    match state.invitation_uc.current(RoleDto { role }).await {
        Ok(v) => (StatusCode::OK, Json(json!({
            "found":       true,
            "url":         v.url,
            "display_url": v.display_url,
            "token":       v.token,
        }))),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

/// 新しい招待トークンを発行します。トランザクション内で処理します。
pub async fn issue(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(query): Query<InvitationQuery>,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }

    let role = match resolve_role(&query) {
        Ok(r)  => r,
        Err(()) => return (StatusCode::BAD_REQUEST, Json(json!({"error": "invalid_role"}))),
    };

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let result = match state.invitation_uc.issue(RoleDto { role }).await {
        Ok(v) => v,
        Err(_) => {
            let _ = tx.rollback().await;
            return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
        }
    };

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({
        "found":       true,
        "url":         result.url,
        "display_url": result.display_url,
        "token":       result.token,
    })))
}
