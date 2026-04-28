//! 認証ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use axum::{
    extract::{Path, Query, State},
    http::StatusCode,
    response::{IntoResponse, Redirect},
    Json,
};
use axum_extra::extract::cookie::{Cookie, SameSite};
use axum_extra::extract::CookieJar;
use serde::Deserialize;
use serde_json::{json, Value};

use crate::{
    state::AppState,
    usecase::auth::dto::LoginDto,
    usecase::invitation::dto::FindByTokenDto,
};
use super::staff_id_from_cookie;

const GOOGLE_TOKEN_URL:    &str = "https://oauth2.googleapis.com/token";
const GOOGLE_USERINFO_URL: &str = "https://www.googleapis.com/oauth2/v2/userinfo";

#[derive(Deserialize)]
pub struct GoogleRedirectQuery {
    token: Option<String>,
}

#[derive(Deserialize)]
pub struct GoogleCallbackQuery {
    code:  Option<String>,
    state: Option<String>,
}

#[derive(Deserialize)]
struct TokenResponse {
    access_token: String,
}

#[derive(Deserialize)]
struct GoogleUserInfo {
    id:      String,
    name:    String,
    email:   String,
    picture: Option<String>,
}

/// Google OAuth リダイレクト URL へ転送します。
pub async fn google_redirect(
    State(state): State<AppState>,
    Query(params): Query<GoogleRedirectQuery>,
) -> Redirect {
    let oauth_state = params.token.as_deref().unwrap_or("state");
    let url = format!(
        "https://accounts.google.com/o/oauth2/auth?client_id={}&redirect_uri={}&response_type=code&scope=email+profile&access_type=online&state={}",
        state.cfg.oauth.google_client_id,
        percent_encoding::utf8_percent_encode(&state.cfg.oauth.google_redirect_url, percent_encoding::NON_ALPHANUMERIC),
        percent_encoding::utf8_percent_encode(oauth_state, percent_encoding::NON_ALPHANUMERIC),
    );
    Redirect::temporary(&url)
}

/// Google OAuth コールバックを処理してセッションを発行します。
pub async fn google_callback(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GoogleCallbackQuery>,
) -> impl IntoResponse {
    let cfg       = &state.cfg;
    let error_url = format!("{}/error?code=500", cfg.app.frontend_url);

    let code = match params.code.filter(|c| !c.is_empty()) {
        Some(c) => c,
        None    => return (jar, Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url))).into_response(),
    };
    let invitation_token = params.state.filter(|s| s != "state" && !s.is_empty());

    let client = reqwest::Client::new();

    let token_resp = client
        .post(GOOGLE_TOKEN_URL)
        .form(&[
            ("client_id",     cfg.oauth.google_client_id.as_str()),
            ("client_secret", cfg.oauth.google_client_secret.as_str()),
            ("redirect_uri",  cfg.oauth.google_redirect_url.as_str()),
            ("code",          code.as_str()),
            ("grant_type",    "authorization_code"),
        ])
        .send().await;

    let token: TokenResponse = match token_resp {
        Ok(r) => match r.json::<TokenResponse>().await {
            Ok(t)  => t,
            Err(e) => { tracing::error!("token parse failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
        },
        Err(e) => { tracing::error!("token exchange failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let userinfo_resp = client
        .get(GOOGLE_USERINFO_URL)
        .bearer_auth(&token.access_token)
        .send().await;

    let user_info: GoogleUserInfo = match userinfo_resp {
        Ok(r) => match r.json::<GoogleUserInfo>().await {
            Ok(u)  => u,
            Err(e) => { tracing::error!("userinfo parse failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
        },
        Err(e) => { tracing::error!("userinfo fetch failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let dto = LoginDto {
        provider:         1,
        provider_id:      user_info.id,
        name:             user_info.name,
        email:            user_info.email,
        avatar:           user_info.picture,
        invitation_token,
    };

    let vo = match state.auth_uc.login(dto).await {
        Ok(v)  => v,
        Err(e) => {
            let msg = e.to_string();
            if msg.contains("invitation_required") {
                return (jar, Redirect::temporary(&format!("{}/error?code=403", cfg.app.frontend_url))).into_response();
            }
            tracing::error!("login failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let max_age = time::Duration::seconds(cfg.app.staff_cookie_lifetime * 60);
    let secure  = cfg.app.env == "production";
    let cookie  = Cookie::build(("staff_id", vo.id.to_string()))
        .path("/")
        .http_only(true)
        .max_age(max_age)
        .same_site(SameSite::Lax)
        .secure(secure)
        .build();

    (jar.add(cookie), Redirect::temporary(&format!("{}/clients", cfg.app.frontend_url))).into_response()
}

/// ログイン中スタッフのプロフィールを返します。
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

/// ログイン中スタッフの情報を返します。
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

/// ログアウトします。
pub async fn logout() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

/// 招待トークンを検証して招待情報を返します。
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
        Err(e) => {
            tracing::error!("invitation find_by_token failed: {}", e);
            (StatusCode::BAD_REQUEST, Json(json!({"error": "invitation_invalid"})))
        }
    }
}
