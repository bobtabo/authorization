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
    pub keyword:   Option<String>,
    pub roles:     Option<String>,
    pub limit:     Option<i64>,
    pub page:      Option<i64>,
    pub sort:      Option<String>,
    pub sort_type: Option<String>,
}

/// スタッフロール更新リクエストボディ。
#[derive(Deserialize)]
pub struct UpdateRoleBody {
    pub role:    i32,
    pub version: i32,
}

/// スタッフ論理削除リクエストボディ。
#[derive(Deserialize)]
pub struct DestroyBody {
    pub version: i32,
}

const DEFAULT_PAGE_COUNT: i64 = 5;

fn build_pager(count: i64, limit: i64, offset: i64, record_count: i64) -> Value {
    let effective_limit = if limit <= 0 { 10 } else { limit };
    let page_count = std::cmp::max(1, (count as f64 / effective_limit as f64).ceil() as i64);
    let last_page_offset = (page_count * effective_limit) - effective_limit;
    let effective_offset = if count > 0 && offset > last_page_offset { last_page_offset } else { offset };
    let page = (effective_offset as f64 / effective_limit as f64).ceil() as i64 + 1;
    let start_page = std::cmp::max(1, page - (DEFAULT_PAGE_COUNT - 1));
    let end_page = std::cmp::min(page_count, start_page + (DEFAULT_PAGE_COUNT - 1));
    json!({
        "count": count,
        "limit": effective_limit,
        "next": page_count > page,
        "previous": page > 1,
        "page": page,
        "nextPage": page + 1,
        "previousPage": page - 1,
        "pageCount": page_count,
        "first": page > 1,
        "last": page_count > page,
        "firstRecordCount": effective_offset + 1,
        "lastRecordCount": effective_offset + record_count,
        "startPage": start_page,
        "endPage": end_page,
    })
}

/// スタッフ一覧を返します。
pub async fn index(
    State(state): State<AppState>,
    Query(q): Query<IndexQuery>,
) -> (StatusCode, Json<Value>) {
    use crate::domain::staff::condition::Condition;
    let limit  = q.limit.unwrap_or(10).max(1);
    let page   = q.page.unwrap_or(1).max(1);
    let offset = limit * (page - 1);
    let roles = q.roles
        .as_deref()
        .map(|s| s.split(',').filter_map(|v| v.trim().parse::<i32>().ok()).collect())
        .unwrap_or_default();
    let cond = Condition {
        keyword:   q.keyword,
        roles,
        offset,
        limit,
        sort:      q.sort,
        sort_type: q.sort_type,
    };
    match state.staff_uc.find_by_condition_with_count(cond).await {
        Ok((staffs, count)) => {
            let data: Vec<Value> = staffs.iter().map(|s| json!({
                "id":         s.id,
                "name":       s.name,
                "email":      s.email,
                "role":       s.role,
                "status":     s.status,
                "created_at": s.created_at.format(TIME_FORMAT).to_string(),
                "updated_at": s.updated_at.format(TIME_FORMAT).to_string(),
            })).collect();
            let pager = build_pager(count, limit, offset, data.len() as i64);
            (StatusCode::OK, Json(json!({"data": data, "pager": pager})))
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

    if let Err(e) = state.staff_uc.update_role(UpdateRoleDto { id, role: body.role, executor_id, version: body.version }).await {
        let _ = tx.rollback().await;
        if e.to_string() == "optimistic_lock_conflict" {
            return (StatusCode::CONFLICT, Json(json!({"error": "optimistic_lock_conflict"})));
        }
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
    Json(body): Json<DestroyBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(e) = state.staff_uc.destroy(DestroyDto { id, executor_id, version: body.version }).await {
        let _ = tx.rollback().await;
        if e.to_string() == "optimistic_lock_conflict" {
            return (StatusCode::CONFLICT, Json(json!({"error": "optimistic_lock_conflict"})));
        }
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({"id": id})))
}
