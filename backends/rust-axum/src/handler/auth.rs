use axum::{
    extract::{Path, State},
    http::StatusCode,
    response::Redirect,
    Json,
};
use axum_extra::extract::CookieJar;
use serde_json::{json, Value};

use crate::{
    state::AppState,
    usecase::invitation::dto::FindByTokenDto,
};
use super::staff_id_from_cookie;

pub async fn google_redirect(State(state): State<AppState>) -> Redirect {
    let url = format!(
        "https://accounts.google.com/o/oauth2/auth?client_id={}&redirect_uri={}&response_type=code&scope=email+profile&access_type=online&state=state",
        state.cfg.oauth.google_client_id,
        state.cfg.oauth.google_redirect_url,
    );
    Redirect::temporary(&url)
}

pub async fn google_callback(State(_state): State<AppState>) -> StatusCode {
    StatusCode::OK
}

pub async fn get_my_profile(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }
    match state.auth_uc.find_user(staff_id).await {
        Ok(s) => (StatusCode::OK, Json(json!({
            "staff_id": s.id,
            "name":     s.name,
            "avatar":   s.avatar,
            "role":     s.role,
        }))),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

pub async fn login(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (StatusCode::UNAUTHORIZED, Json(json!({"error": "unauthenticated"})));
    }
    match state.auth_uc.find_user(staff_id).await {
        Ok(s) => (StatusCode::OK, Json(json!({
            "staff_id": s.id,
            "name":     s.name,
            "avatar":   s.avatar,
            "role":     s.role,
        }))),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

pub async fn logout() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn invitation(
    State(state): State<AppState>,
    Path(token): Path<String>,
) -> (StatusCode, Json<Value>) {
    match state.invitation_uc.find_by_token(FindByTokenDto { token }).await {
        Ok(v) => (StatusCode::OK, Json(json!({
            "found":       true,
            "url":         v.url,
            "display_url": v.display_url,
            "token":       v.token,
        }))),
        Err(_) => (StatusCode::BAD_REQUEST, Json(json!({"error": "invitation_invalid"}))),
    }
}
