//! 招待管理ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{extract::State, http::StatusCode, Json};
use serde_json::{json, Value};

use crate::state::AppState;

/// 現在の招待トークン情報を返します。
pub async fn index(State(state): State<AppState>) -> (StatusCode, Json<Value>) {
    match state.invitation_uc.current().await {
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
pub async fn issue(State(state): State<AppState>) -> (StatusCode, Json<Value>) {
    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let result = match state.invitation_uc.issue().await {
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
