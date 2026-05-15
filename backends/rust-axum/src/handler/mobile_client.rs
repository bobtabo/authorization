//! スマホ連携クライアントハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{
    extract::{Path, State},
    http::StatusCode,
    Json,
};
use serde_json::{json, Value};

use crate::state::AppState;

/// QRコードデータを返します。
pub async fn qr(
    Path(identifier): Path<String>,
) -> (StatusCode, Json<Value>) {
    let deeplink_url = "authgateway://clients/".to_string() + &identifier + "/info";
    (StatusCode::OK, Json(json!({
        "identifier":   identifier,
        "deeplink_url": deeplink_url,
    })))
}

/// クライアント情報を返します。識別子で DB 検索し、見つからなければ 404 を返します。
pub async fn info(
    State(state): State<AppState>,
    Path(identifier): Path<String>,
) -> (StatusCode, Json<Value>) {
    match state.client_uc.find_mobile_info_by_identifier(&identifier).await {
        Ok(vo) => (StatusCode::OK, Json(json!({
            "identifier": vo.identifier,
            "name":       vo.name,
            "status":     vo.status,
        }))),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

/// 利用開始処理。アクセストークンを返します。識別子で DB 検索し、見つからなければ 404 を返します。
pub async fn start(
    State(state): State<AppState>,
    Path(identifier): Path<String>,
) -> (StatusCode, Json<Value>) {
    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let result = match state.client_uc.start(&identifier).await {
        Ok(vo) => vo,
        Err(e) => {
            let _ = tx.rollback().await;
            if e.to_string() == "client_not_found" {
                return (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"})));
            }
            return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
        }
    };

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"access_token": result.access_token})))
}

/// 利用停止処理。Active なら Suspended に変更します。識別子で DB 検索し、見つからなければ 404 を返します。
pub async fn stop(
    State(state): State<AppState>,
    Path(identifier): Path<String>,
) -> (StatusCode, Json<Value>) {
    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(e) = state.client_uc.stop(&identifier).await {
        let _ = tx.rollback().await;
        if e.to_string() == "client_not_found" {
            return (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"})));
        }
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({})))
}
