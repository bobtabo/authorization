//! スタッフハンドラーモジュール。
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

use crate::{
    state::AppState,
    usecase::staff::dto::{UpdateRoleDto, DestroyDto},
};
use super::{staff_id_from_cookie, TIME_FORMAT};

/// スタッフ一覧取得クエリ。
#[derive(Deserialize)]
pub struct IndexQuery {
    pub keyword: Option<String>,
    pub roles:   Option<String>,
}

/// スタッフロール更新リクエストボディ。
#[derive(Deserialize)]
pub struct UpdateRoleBody {
    pub role: i32,
}

/// スタッフ一覧を返します。
pub async fn index(
    State(state): State<AppState>,
    Query(q): Query<IndexQuery>,
) -> (StatusCode, Json<Value>) {
    use crate::domain::staff::condition::Condition;
    let roles = q.roles
        .as_deref()
        .map(|s| s.split(',').filter_map(|v| v.trim().parse::<i32>().ok()).collect())
        .unwrap_or_default();
    let cond = Condition { keyword: q.keyword, roles };
    match state.staff_uc.find_by_condition(cond).await {
        Ok(staffs) => {
            let items: Vec<Value> = staffs.iter().map(|s| json!({
                "id":         s.id,
                "name":       s.name,
                "email":      s.email,
                "role":       s.role,
                "status":     s.status,
                "created_at": s.created_at.format(TIME_FORMAT).to_string(),
                "updated_at": s.updated_at.format(TIME_FORMAT).to_string(),
            })).collect();
            (StatusCode::OK, Json(json!({"items": items})))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

/// スタッフのロールを更新します。トランザクション内で処理します。
pub async fn update_role(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u32>,
    Json(body): Json<UpdateRoleBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(_) = state.staff_uc.update_role(UpdateRoleDto { id, role: body.role, executor_id }).await {
        let _ = tx.rollback().await;
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"id": id})))
}

/// スタッフの論理削除を復元します。トランザクション内で処理します。
pub async fn restore(
    State(state): State<AppState>,
    Path(id): Path<u32>,
) -> (StatusCode, Json<Value>) {
    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(_) = state.staff_uc.restore(id).await {
        let _ = tx.rollback().await;
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"id": id})))
}

/// スタッフを論理削除します。トランザクション内で処理します。
pub async fn destroy(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u32>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(_) = state.staff_uc.destroy(DestroyDto { id, executor_id }).await {
        let _ = tx.rollback().await;
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"id": id})))
}
