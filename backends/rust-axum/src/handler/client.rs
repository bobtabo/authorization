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
    usecase::{
        client::dto::{ListConditionDto, StoreDto, UpdateDto},
        notification::dto::FanOutDto,
    },
};
use super::{staff_id_from_cookie, TIME_FORMAT};

#[derive(Deserialize)]
pub struct IndexQuery {
    pub keyword:    Option<String>,
    pub start_from: Option<String>,
    pub start_to:   Option<String>,
}

#[derive(Deserialize)]
pub struct StoreBody {
    pub name:      String,
    pub post_code: Option<String>,
    pub pref:      Option<String>,
    pub city:      Option<String>,
    pub address:   Option<String>,
    pub building:  Option<String>,
    pub tel:       Option<String>,
    pub email:     Option<String>,
}

#[derive(Deserialize)]
pub struct UpdateBody {
    pub name:      Option<String>,
    pub post_code: Option<String>,
    pub pref:      Option<String>,
    pub city:      Option<String>,
    pub address:   Option<String>,
    pub building:  Option<String>,
    pub tel:       Option<String>,
    pub email:     Option<String>,
    pub status:    Option<i32>,
}

pub async fn index(
    State(state): State<AppState>,
    Query(q): Query<IndexQuery>,
) -> (StatusCode, Json<Value>) {
    let dto = ListConditionDto {
        keyword:    q.keyword,
        start_from: q.start_from,
        start_to:   q.start_to,
        statuses:   vec![],
    };
    match state.client_uc.find_by_condition(dto).await {
        Ok(clients) => {
            let list: Vec<Value> = clients.iter().map(|c| json!({
                "id":         c.id,
                "name":       c.name,
                "status":     c.status,
                "start_at":   c.start_at.map(|t| t.format(TIME_FORMAT).to_string()),
                "stop_at":    c.stop_at.map(|t| t.format(TIME_FORMAT).to_string()),
                "created_at": c.created_at.format(TIME_FORMAT).to_string(),
                "updated_at": c.updated_at.format(TIME_FORMAT).to_string(),
            })).collect();
            (StatusCode::OK, Json(json!(list)))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn show(
    State(state): State<AppState>,
    Path(id): Path<u64>,
) -> (StatusCode, Json<Value>) {
    match state.client_uc.find_by_id(id).await {
        Ok(c) => (StatusCode::OK, Json(json!({
            "id":         c.id,
            "name":       c.name,
            "identifier": c.identifier,
            "post_code":  c.post_code,
            "pref":       c.pref,
            "city":       c.city,
            "address":    c.address,
            "building":   c.building,
            "tel":        c.tel,
            "email":      c.email,
            "status":     c.status,
            "start_at":   c.start_at.map(|t| t.format(TIME_FORMAT).to_string()),
            "stop_at":    c.stop_at.map(|t| t.format(TIME_FORMAT).to_string()),
            "created_at": c.created_at.format(TIME_FORMAT).to_string(),
            "updated_at": c.updated_at.format(TIME_FORMAT).to_string(),
        }))),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

pub async fn store(
    State(state): State<AppState>,
    jar: CookieJar,
    Json(body): Json<StoreBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);
    let dto = StoreDto {
        name:        body.name,
        post_code:   body.post_code.unwrap_or_default(),
        pref:        body.pref.unwrap_or_default(),
        city:        body.city.unwrap_or_default(),
        address:     body.address.unwrap_or_default(),
        building:    body.building.unwrap_or_default(),
        tel:         body.tel.unwrap_or_default(),
        email:       body.email.unwrap_or_default(),
        executor_id,
    };
    match state.client_uc.store(dto).await {
        Ok(client) => {
            let notif_url = format!("/clients/show?id={}", client.id);
            let _ = state.notification_uc.fan_out(FanOutDto {
                title:        "新しいクライアントが登録されました".to_string(),
                message:      client.name.clone(),
                message_type: 1,
                executor_id,
                url:          notif_url,
            }).await;
            let mailer = state.mailer.clone();
            let email = client.email.clone();
            let name = client.name.clone();
            let token = client.access_token.clone();
            tokio::spawn(async move {
                mailer.send_access_token(&email, &name, &token).await;
            });
            (StatusCode::CREATED, Json(json!({"id": client.id})))
        }
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn update(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u64>,
    Json(body): Json<UpdateBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);
    let dto = UpdateDto {
        id,
        name:        body.name,
        post_code:   body.post_code,
        pref:        body.pref,
        city:        body.city,
        address:     body.address,
        building:    body.building,
        tel:         body.tel,
        email:       body.email,
        status:      body.status,
        executor_id,
    };
    match state.client_uc.update(dto).await {
        Ok(c) => (StatusCode::OK, Json(json!({
            "id":         c.id,
            "name":       c.name,
            "identifier": c.identifier,
            "post_code":  c.post_code,
            "pref":       c.pref,
            "city":       c.city,
            "address":    c.address,
            "building":   c.building,
            "tel":        c.tel,
            "email":      c.email,
            "status":     c.status,
            "start_at":   c.start_at.map(|t| t.format(TIME_FORMAT).to_string()),
            "stop_at":    c.stop_at.map(|t| t.format(TIME_FORMAT).to_string()),
            "created_at": c.created_at.format(TIME_FORMAT).to_string(),
            "updated_at": c.updated_at.format(TIME_FORMAT).to_string(),
        }))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}

pub async fn destroy(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u64>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);
    match state.client_uc.destroy(id, executor_id).await {
        Ok(_) => (StatusCode::OK, Json(json!({}))),
        Err(_) => (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    }
}
