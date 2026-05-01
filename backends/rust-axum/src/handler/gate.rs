//! Gate ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{
    extract::{Path, Query, State},
    http::{header::AUTHORIZATION, HeaderMap, StatusCode},
    Json,
};
use serde::Deserialize;
use serde_json::{json, Value};

use crate::{
    state::AppState,
    usecase::gate::dto::{IssueDto, VerifyDto},
};

/// JWT 発行クエリ。
#[derive(Deserialize)]
pub struct IssueQuery {
    pub member: Option<String>,
}

/// JWT 検証クエリ。
#[derive(Deserialize)]
pub struct VerifyQuery {
    pub token: Option<String>,
}

/// アクセストークンを検証して JWT を発行します。
pub async fn issue(
    State(state): State<AppState>,
    headers: HeaderMap,
    Query(q): Query<IssueQuery>,
) -> (StatusCode, Json<Value>) {
    let member = match q.member {
        Some(m) if !m.is_empty() => m,
        _ => return (StatusCode::BAD_REQUEST, Json(json!({"error": "member_required"}))),
    };
    let auth = headers.get(AUTHORIZATION)
        .and_then(|v| v.to_str().ok())
        .unwrap_or("");
    let access_token = auth.strip_prefix("Bearer ").unwrap_or("").to_string();

    match state.gate_uc.issue_token(IssueDto { access_token, member_id: member }).await {
        Ok(vo)  => (StatusCode::OK, Json(json!({"token": vo.token}))),
        Err(_)  => (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthorized"}))),
    }
}

/// JWT を検証してクレームを返します。
pub async fn verify(
    State(state): State<AppState>,
    Path(identifier): Path<String>,
    Query(q): Query<VerifyQuery>,
) -> (StatusCode, Json<Value>) {
    let token = match q.token {
        Some(t) if !t.is_empty() => t,
        _ => return (StatusCode::BAD_REQUEST, Json(json!({"error": "token_required"}))),
    };
    match state.gate_uc.verify(VerifyDto { identifier, token }).await {
        Ok(vo)  => (StatusCode::OK, Json(vo.claims)),
        Err(_)  => (StatusCode::UNAUTHORIZED, Json(json!({"error": "jwt_invalid"}))),
    }
}
