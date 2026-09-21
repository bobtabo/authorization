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

use super::staff_id_from_cookie;
use crate::{
    state::AppState, usecase::auth::dto::LoginDto, usecase::invitation::dto::FindByTokenDto,
};

const GOOGLE_TOKEN_URL: &str = "https://oauth2.googleapis.com/token";
const GOOGLE_USERINFO_URL: &str = "https://www.googleapis.com/oauth2/v2/userinfo";

const GITHUB_AUTH_URL: &str = "https://github.com/login/oauth/authorize";
const GITHUB_TOKEN_URL: &str = "https://github.com/login/oauth/access_token";
const GITHUB_USER_URL: &str = "https://api.github.com/user";
const GITHUB_EMAILS_URL: &str = "https://api.github.com/user/emails";

/// OAuth 認可開始時に発行する nonce を保持するクッキー名。
const OAUTH_STATE_COOKIE: &str = "oauth_state";
/// nonce クッキーの有効期間（秒）。
const OAUTH_STATE_COOKIE_MAX_AGE: i64 = 600;

/// nonce を生成してクッキーに保存し、state（"{runtime}|{nonce}" または "{runtime}|{nonce}|{token}"）を返します。
fn issue_oauth_state(
    cfg: &crate::config::Config,
    jar: CookieJar,
    token: Option<&str>,
) -> (CookieJar, String) {
    let nonce = hex::encode(rand::random::<[u8; 16]>());
    let cookie = Cookie::build((OAUTH_STATE_COOKIE, nonce.clone()))
        .path("/")
        .http_only(true)
        .max_age(time::Duration::seconds(OAUTH_STATE_COOKIE_MAX_AGE))
        .same_site(SameSite::Lax)
        .secure(cfg.app.env == "production")
        .build();
    let oauth_state = match token.filter(|t| !t.is_empty()) {
        Some(token) => format!("{}|{}|{}", cfg.app.runtime, nonce, token),
        None => format!("{}|{}", cfg.app.runtime, nonce),
    };
    (jar.add(cookie), oauth_state)
}

/// state の nonce をクッキーと照合してクッキーを破棄し、招待トークンを返します。
/// 照合に失敗した場合は `None` を返します。
fn consume_oauth_state(jar: CookieJar, state: Option<&str>) -> (CookieJar, Option<Option<String>>) {
    let saved = jar
        .get(OAUTH_STATE_COOKIE)
        .map(|c| c.value().to_string())
        .unwrap_or_default();
    let jar = jar.remove(Cookie::build(OAUTH_STATE_COOKIE).path("/").build());

    let mut parts = state.unwrap_or_default().splitn(3, '|');
    let _runtime = parts.next();
    let nonce = parts.next().unwrap_or_default();
    if saved.is_empty() || nonce.is_empty() || !constant_time_eq(saved.as_bytes(), nonce.as_bytes()) {
        return (jar, None);
    }
    let invitation_token = parts.next().filter(|t| !t.is_empty()).map(|t| t.to_string());
    (jar, Some(invitation_token))
}

/// 定数時間でバイト列を比較します。
fn constant_time_eq(a: &[u8], b: &[u8]) -> bool {
    if a.len() != b.len() {
        return false;
    }
    a.iter().zip(b).fold(0u8, |acc, (x, y)| acc | (x ^ y)) == 0
}

#[derive(Deserialize)]
pub struct GoogleRedirectQuery {
    token: Option<String>,
}

#[derive(Deserialize)]
pub struct GoogleCallbackQuery {
    code: Option<String>,
    state: Option<String>,
}

#[derive(Deserialize)]
pub struct GithubRedirectQuery {
    token: Option<String>,
}

#[derive(Deserialize)]
pub struct GithubCallbackQuery {
    code: Option<String>,
    state: Option<String>,
}

#[derive(Deserialize)]
struct TokenResponse {
    access_token: String,
}

#[derive(Deserialize)]
struct GoogleUserInfo {
    id: String,
    name: String,
    email: String,
    picture: Option<String>,
}

#[derive(Deserialize)]
struct GithubUserInfo {
    id: i64,
    login: String,
    name: Option<String>,
    avatar_url: Option<String>,
}

#[derive(Deserialize)]
struct GithubEmail {
    email: String,
    primary: bool,
    verified: bool,
}

/// Google OAuth リダイレクト URL へ転送します。
pub async fn google_redirect(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GoogleRedirectQuery>,
) -> (CookieJar, Redirect) {
    let (jar, oauth_state) = issue_oauth_state(&state.cfg, jar, params.token.as_deref());
    let url = format!(
        "https://accounts.google.com/o/oauth2/auth?client_id={}&redirect_uri={}&response_type=code&scope=email+profile&access_type=online&state={}",
        state.cfg.oauth.google_client_id,
        percent_encoding::utf8_percent_encode(&state.cfg.oauth.google_redirect_url, percent_encoding::NON_ALPHANUMERIC),
        percent_encoding::utf8_percent_encode(&oauth_state, percent_encoding::NON_ALPHANUMERIC),
    );
    (jar, Redirect::temporary(&url))
}

/// Google OAuth コールバックを処理してセッションを発行します。
pub async fn google_callback(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GoogleCallbackQuery>,
) -> impl IntoResponse {
    let cfg = &state.cfg;
    let error_url = format!("{}/error?code=500", cfg.app.frontend_url);

    let (jar, invitation_token) = match consume_oauth_state(jar, params.state.as_deref()) {
        (jar, Some(token)) => (jar, token),
        (jar, None) => {
            return (
                jar,
                Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url)),
            )
                .into_response()
        }
    };
    let code = match params.code.filter(|c| !c.is_empty()) {
        Some(c) => c,
        None => {
            return (
                jar,
                Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url)),
            )
                .into_response()
        }
    };

    let client = reqwest::Client::new();

    let token_resp = client
        .post(GOOGLE_TOKEN_URL)
        .form(&[
            ("client_id", cfg.oauth.google_client_id.as_str()),
            ("client_secret", cfg.oauth.google_client_secret.as_str()),
            ("redirect_uri", cfg.oauth.google_redirect_url.as_str()),
            ("code", code.as_str()),
            ("grant_type", "authorization_code"),
        ])
        .send()
        .await;

    let token: TokenResponse = match token_resp {
        Ok(r) => match r.json::<TokenResponse>().await {
            Ok(t) => t,
            Err(e) => {
                tracing::error!("token parse failed: {}", e);
                return (jar, Redirect::temporary(&error_url)).into_response();
            }
        },
        Err(e) => {
            tracing::error!("token exchange failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let userinfo_resp = client
        .get(GOOGLE_USERINFO_URL)
        .bearer_auth(&token.access_token)
        .send()
        .await;

    let user_info: GoogleUserInfo = match userinfo_resp {
        Ok(r) => match r.json::<GoogleUserInfo>().await {
            Ok(u) => u,
            Err(e) => {
                tracing::error!("userinfo parse failed: {}", e);
                return (jar, Redirect::temporary(&error_url)).into_response();
            }
        },
        Err(e) => {
            tracing::error!("userinfo fetch failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let dto = LoginDto {
        provider: 1,
        provider_id: user_info.id,
        name: user_info.name,
        email: user_info.email,
        avatar: user_info.picture,
        invitation_token,
    };

    let vo = match state.auth_uc.login(dto).await {
        Ok(v) => v,
        Err(e) => {
            let msg = e.to_string();
            if msg.contains("invitation_required") {
                return (
                    jar,
                    Redirect::temporary(&format!("{}/error?code=403", cfg.app.frontend_url)),
                )
                    .into_response();
            }
            tracing::error!("login failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let max_age = time::Duration::seconds(cfg.app.staff_cookie_lifetime * 60);
    let secure = cfg.app.env == "production";
    let cookie = Cookie::build(("staff_id", vo.id.to_string()))
        .path("/")
        .http_only(true)
        .max_age(max_age)
        .same_site(SameSite::Lax)
        .secure(secure)
        .build();

    (
        jar.add(cookie),
        Redirect::temporary(&format!("{}/clients", cfg.app.frontend_url)),
    )
        .into_response()
}

/// GitHub OAuth リダイレクト URL へ転送します。
pub async fn github_redirect(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GithubRedirectQuery>,
) -> (CookieJar, Redirect) {
    let (jar, oauth_state) = issue_oauth_state(&state.cfg, jar, params.token.as_deref());
    let url = format!(
        "{}?client_id={}&redirect_uri={}&scope=user:email&state={}",
        GITHUB_AUTH_URL,
        state.cfg.oauth.github_client_id,
        percent_encoding::utf8_percent_encode(
            &state.cfg.oauth.github_redirect_url,
            percent_encoding::NON_ALPHANUMERIC
        ),
        percent_encoding::utf8_percent_encode(&oauth_state, percent_encoding::NON_ALPHANUMERIC),
    );
    (jar, Redirect::temporary(&url))
}

/// GitHub OAuth コールバックを処理してセッションを発行します。
pub async fn github_callback(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GithubCallbackQuery>,
) -> impl IntoResponse {
    let cfg = &state.cfg;
    let error_url = format!("{}/error?code=500", cfg.app.frontend_url);

    let (jar, invitation_token) = match consume_oauth_state(jar, params.state.as_deref()) {
        (jar, Some(token)) => (jar, token),
        (jar, None) => {
            return (
                jar,
                Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url)),
            )
                .into_response()
        }
    };
    let code = match params.code.filter(|c| !c.is_empty()) {
        Some(c) => c,
        None => {
            return (
                jar,
                Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url)),
            )
                .into_response()
        }
    };

    let client = reqwest::Client::builder()
        .user_agent("authorization-app")
        .build()
        .unwrap_or_default();

    let token_resp = client
        .post(GITHUB_TOKEN_URL)
        .header("Accept", "application/json")
        .form(&[
            ("client_id", cfg.oauth.github_client_id.as_str()),
            ("client_secret", cfg.oauth.github_client_secret.as_str()),
            ("redirect_uri", cfg.oauth.github_redirect_url.as_str()),
            ("code", code.as_str()),
        ])
        .send()
        .await;

    let token: TokenResponse = match token_resp {
        Ok(r) => match r.json::<TokenResponse>().await {
            Ok(t) => t,
            Err(e) => {
                tracing::error!("github token parse failed: {}", e);
                return (jar, Redirect::temporary(&error_url)).into_response();
            }
        },
        Err(e) => {
            tracing::error!("github token exchange failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let user_resp = client
        .get(GITHUB_USER_URL)
        .bearer_auth(&token.access_token)
        .header("Accept", "application/json")
        .send()
        .await;

    let github_user: GithubUserInfo = match user_resp {
        Ok(r) => match r.json::<GithubUserInfo>().await {
            Ok(u) => u,
            Err(e) => {
                tracing::error!("github user parse failed: {}", e);
                return (jar, Redirect::temporary(&error_url)).into_response();
            }
        },
        Err(e) => {
            tracing::error!("github user fetch failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let emails_resp = client
        .get(GITHUB_EMAILS_URL)
        .bearer_auth(&token.access_token)
        .header("Accept", "application/json")
        .send()
        .await;

    let emails: Vec<GithubEmail> = match emails_resp {
        Ok(r) => match r.json::<Vec<GithubEmail>>().await {
            Ok(e) => e,
            Err(e) => {
                tracing::error!("github emails parse failed: {}", e);
                return (jar, Redirect::temporary(&error_url)).into_response();
            }
        },
        Err(e) => {
            tracing::error!("github emails fetch failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let email = match emails.into_iter().find(|e| e.primary && e.verified) {
        Some(e) => e.email,
        None => {
            tracing::error!("github primary email not found");
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let name = github_user.name.unwrap_or(github_user.login);

    let dto = LoginDto {
        provider: 2,
        provider_id: github_user.id.to_string(),
        name,
        email,
        avatar: github_user.avatar_url,
        invitation_token,
    };

    let vo = match state.auth_uc.login(dto).await {
        Ok(v) => v,
        Err(e) => {
            let msg = e.to_string();
            if msg.contains("invitation_required") {
                return (
                    jar,
                    Redirect::temporary(&format!("{}/error?code=403", cfg.app.frontend_url)),
                )
                    .into_response();
            }
            tracing::error!("github login failed: {}", e);
            return (jar, Redirect::temporary(&error_url)).into_response();
        }
    };

    let max_age = time::Duration::seconds(cfg.app.staff_cookie_lifetime * 60);
    let secure = cfg.app.env == "production";
    let cookie = Cookie::build(("staff_id", vo.id.to_string()))
        .path("/")
        .http_only(true)
        .max_age(max_age)
        .same_site(SameSite::Lax)
        .secure(secure)
        .build();

    (
        jar.add(cookie),
        Redirect::temporary(&format!("{}/clients", cfg.app.frontend_url)),
    )
        .into_response()
}

/// ログイン中スタッフのプロフィールを返します。
pub async fn get_my_profile(
    State(state): State<AppState>,
    jar: CookieJar,
) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (
            StatusCode::UNAUTHORIZED,
            Json(json!({"error": "unauthenticated"})),
        );
    }
    match state.auth_uc.find_user(staff_id).await {
        Ok(s) => (
            StatusCode::OK,
            Json(json!({
                "staff_id": s.id,
                "name":     s.name,
                "avatar":   s.avatar,
                "role":     s.role,
            })),
        ),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

/// ログイン中スタッフの情報を返します。
pub async fn login(State(state): State<AppState>, jar: CookieJar) -> (StatusCode, Json<Value>) {
    let staff_id = staff_id_from_cookie(&jar);
    if staff_id == 0 {
        return (
            StatusCode::UNAUTHORIZED,
            Json(json!({"error": "unauthenticated"})),
        );
    }
    match state.auth_uc.find_user(staff_id).await {
        Ok(s) => (
            StatusCode::OK,
            Json(json!({
                "staff_id": s.id,
                "name":     s.name,
                "avatar":   s.avatar,
                "role":     s.role,
            })),
        ),
        Err(_) => (StatusCode::NOT_FOUND, Json(json!({"error": "not_found"}))),
    }
}

/// ログアウトします。
pub async fn logout(jar: CookieJar) -> (CookieJar, (StatusCode, Json<Value>)) {
    let expired = Cookie::build(("staff_id", ""))
        .path("/")
        .http_only(true)
        .max_age(time::Duration::ZERO)
        .build();
    (jar.add(expired), (StatusCode::OK, Json(json!({}))))
}

/// 招待トークンを検証して招待情報を返します。
pub async fn invitation(
    State(state): State<AppState>,
    Path(token): Path<String>,
) -> (StatusCode, Json<Value>) {
    match state
        .invitation_uc
        .find_by_token(FindByTokenDto { token })
        .await
    {
        Ok(v) => (
            StatusCode::OK,
            Json(json!({
                "found":       true,
                "url":         v.url,
                "display_url": v.display_url,
                "token":       v.token,
            })),
        ),
        Err(e) => {
            tracing::error!("invitation find_by_token failed: {}", e);
            (
                StatusCode::BAD_REQUEST,
                Json(json!({"error": "invitation_invalid"})),
            )
        }
    }
}
