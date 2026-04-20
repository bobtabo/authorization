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

#[derive(Deserialize)]
pub struct IssueQuery {
    pub member: Option<String>,
}

#[derive(Deserialize)]
pub struct VerifyQuery {
    pub token: Option<String>,
}

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
        Ok(token) => (StatusCode::OK, Json(json!({"token": token}))),
        Err(_)    => (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthorized"}))),
    }
}

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
        Ok(payload) => (StatusCode::OK, Json(payload)),
        Err(_)      => (StatusCode::UNAUTHORIZED, Json(json!({"error": "jwt_invalid"}))),
    }
}
