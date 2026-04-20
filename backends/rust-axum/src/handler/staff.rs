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

#[derive(Deserialize)]
pub struct IndexQuery {
    pub keyword: Option<String>,
    pub roles:   Option<String>,
}

#[derive(Deserialize)]
pub struct UpdateRoleBody {
    pub role: i32,
}

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
                "status":     crate::usecase::staff::Interactor::status(s),
                "created_at": s.created_at.format(TIME_FORMAT).to_string(),
                "updated_at": s.updated_at.format(TIME_FORMAT).to_string(),
            })).collect();
            (StatusCode::OK, Json(json!({"items": items})))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn update_role(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u32>,
    Json(body): Json<UpdateRoleBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);
    match state.staff_uc.update_role(UpdateRoleDto { id, role: body.role, executor_id }).await {
        Ok(_)  => (StatusCode::OK, Json(json!({"id": id}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn restore(
    State(state): State<AppState>,
    Path(id): Path<u32>,
) -> (StatusCode, Json<Value>) {
    match state.staff_uc.restore(id).await {
        Ok(_)  => (StatusCode::OK, Json(json!({"id": id}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn destroy(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u32>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);
    match state.staff_uc.destroy(DestroyDto { id, executor_id }).await {
        Ok(_)  => (StatusCode::OK, Json(json!({"id": id}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}
