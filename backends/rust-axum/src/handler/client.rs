//! クライアントハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{
    extract::{Path, Query, State},
    http::StatusCode,
    Json,
};
use axum_extra::extract::CookieJar;
use garde::Validate;
use regex::Regex;
use std::sync::LazyLock;
use serde::Deserialize;
use serde_json::{json, Value};

static TEL_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^\d{10,11}$").unwrap());
static EMAIL_REGEX: LazyLock<Regex> = LazyLock::new(|| Regex::new(r"^[^\s@]+@[^\s@]+\.[^\s@]+$").unwrap());

use crate::{
    state::AppState,
    usecase::{
        client::dto::{ListConditionDto, StoreDto, UpdateDto},
        notification::dto::FanOutDto,
    },
};
use super::{staff_id_from_cookie, TIME_FORMAT};

/// クライアント一覧取得クエリ。
#[derive(Deserialize)]
pub struct IndexQuery {
    pub keyword:    Option<String>,
    pub start_from: Option<String>,
    pub start_to:   Option<String>,
}

/// クライアント登録リクエストボディ。
#[derive(Deserialize, Validate)]
pub struct StoreBody {
    #[garde(length(max = 255))]
    pub name:      String,
    #[garde(length(max = 8))]
    pub post_code: String,
    #[garde(length(max = 50))]
    pub pref:      String,
    #[garde(length(max = 100))]
    pub city:      String,
    #[garde(length(max = 255))]
    pub address:   String,
    #[garde(length(max = 255))]
    pub building:  Option<String>,
    #[garde(pattern(TEL_REGEX))]
    pub tel:       String,
    #[garde(pattern(EMAIL_REGEX), length(max = 255))]
    pub email:     String,
}

/// クライアント更新リクエストボディ。
#[derive(Deserialize, Validate)]
pub struct UpdateBody {
    #[garde(inner(length(max = 255)))]
    pub name:      Option<String>,
    #[garde(inner(length(max = 8)))]
    pub post_code: Option<String>,
    #[garde(inner(length(max = 50)))]
    pub pref:      Option<String>,
    #[garde(inner(length(max = 100)))]
    pub city:      Option<String>,
    #[garde(inner(length(max = 255)))]
    pub address:   Option<String>,
    #[garde(inner(length(max = 255)))]
    pub building:  Option<String>,
    #[garde(inner(pattern(TEL_REGEX)))]
    pub tel:       Option<String>,
    #[garde(inner(pattern(EMAIL_REGEX), length(max = 255)))]
    pub email:     Option<String>,
    #[garde(skip)]
    pub status:    Option<i32>,
    #[garde(skip)]
    pub version:   i32,
}

/// クライアント論理削除リクエストボディ。
#[derive(Deserialize)]
pub struct DestroyBody {
    pub version: i32,
}

/// クライアント一覧を返します。
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
        Err(e) => {
            tracing::error!("client index error: {e}");
            (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})))
        }
    }
}

/// クライアント詳細を返します。
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

/// クライアントを登録します。トランザクション内で処理し、完了後にメール送信・通知配信を行います。
pub async fn store(
    State(state): State<AppState>,
    jar: CookieJar,
    Json(body): Json<StoreBody>,
) -> (StatusCode, Json<Value>) {
    if body.validate().is_err() {
        return (StatusCode::UNPROCESSABLE_ENTITY, Json(json!({"error": "validation_error"})));
    }
    let executor_id = staff_id_from_cookie(&jar);
    let dto = StoreDto {
        name:        body.name,
        post_code:   body.post_code,
        pref:        body.pref,
        city:        body.city,
        address:     body.address,
        building:    body.building.unwrap_or_default(),
        tel:         body.tel,
        email:       body.email,
        executor_id,
    };

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let result = match state.client_uc.store(dto).await {
        Ok(r) => r,
        Err(_) => {
            let _ = tx.rollback().await;
            return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
        }
    };

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    // トランザクション外でのサイドエフェクト
    let notif_url = format!("/clients/show?id={}", result.id);
    let _ = state.notification_uc.fan_out(FanOutDto {
        title:        "新しいクライアントが登録されました".to_string(),
        message:      result.name.clone(),
        message_type: 1,
        executor_id,
        url:          notif_url,
    }).await;
    let mailer = state.mailer.clone();
    let email = result.email.clone();
    let name = result.name.clone();
    let activate_url = format!("{}/clients/{}/qr", state.cfg.app.frontend_url, result.identifier);
    tokio::spawn(async move {
        mailer.send_activation(&email, &name, &activate_url).await;
    });

    (StatusCode::CREATED, Json(json!({"id": result.id})))
}

/// クライアント情報を更新します。トランザクション内で処理します。
pub async fn update(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u64>,
    Json(body): Json<UpdateBody>,
) -> (StatusCode, Json<Value>) {
    if body.validate().is_err() {
        return (StatusCode::UNPROCESSABLE_ENTITY, Json(json!({"error": "validation_error"})));
    }
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
        version:     body.version,
    };

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    let result = match state.client_uc.update(dto).await {
        Ok(r) => r,
        Err(e) => {
            let _ = tx.rollback().await;
            if e.to_string() == "optimistic_lock_conflict" {
                return (StatusCode::CONFLICT, Json(json!({"error": "optimistic_lock_conflict"})));
            }
            return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
        }
    };

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({
        "id":         result.id,
        "name":       result.name,
        "identifier": result.identifier,
        "post_code":  result.post_code,
        "pref":       result.pref,
        "city":       result.city,
        "address":    result.address,
        "building":   result.building,
        "tel":        result.tel,
        "email":      result.email,
        "status":     result.status,
        "start_at":   result.start_at.map(|t| t.format(TIME_FORMAT).to_string()),
        "stop_at":    result.stop_at.map(|t| t.format(TIME_FORMAT).to_string()),
        "created_at": result.created_at.format(TIME_FORMAT).to_string(),
        "updated_at": result.updated_at.format(TIME_FORMAT).to_string(),
    })))
}

/// クライアントを論理削除します。トランザクション内で処理します。
pub async fn destroy(
    State(state): State<AppState>,
    jar: CookieJar,
    Path(id): Path<u64>,
    Json(body): Json<DestroyBody>,
) -> (StatusCode, Json<Value>) {
    let executor_id = staff_id_from_cookie(&jar);

    let tx = match state.pool.begin().await {
        Ok(tx) => tx,
        Err(_) => return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"}))),
    };

    if let Err(e) = state.client_uc.destroy(id, executor_id, body.version).await {
        let _ = tx.rollback().await;
        if e.to_string() == "optimistic_lock_conflict" {
            return (StatusCode::CONFLICT, Json(json!({"error": "optimistic_lock_conflict"})));
        }
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    if tx.commit().await.is_err() {
        return (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})));
    }

    (StatusCode::OK, Json(json!({})))
}

/// 指定したクライアント ID の JWT 履歴一覧を返します。
pub async fn jwt_histories(
    State(state): State<AppState>,
    Path(id): Path<u64>,
) -> (StatusCode, Json<Value>) {
    match state.jwt_history_repo.find_by_client_id(id).await {
        Ok(histories) => {
            let list: Vec<Value> = histories.iter().map(|h| json!({
                "id":        h.id,
                "member_id": h.member_id,
                "issue_at":  h.issue_at.format("%Y-%m-%d %H:%M:%S").to_string(),
                "jwt":       h.jwt,
            })).collect();
            (StatusCode::OK, Json(json!(list)))
        }
        Err(e) => {
            tracing::error!("jwt_histories error: {e}");
            (StatusCode::INTERNAL_SERVER_ERROR, Json(json!({"error": "internal_error"})))
        }
    }
}
