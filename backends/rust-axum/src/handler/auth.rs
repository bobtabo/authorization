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

const GITHUB_AUTH_URL:    &str = "https://github.com/login/oauth/authorize";
const GITHUB_TOKEN_URL:   &str = "https://github.com/login/oauth/access_token";
const GITHUB_USER_URL:    &str = "https://api.github.com/user";
const GITHUB_EMAILS_URL:  &str = "https://api.github.com/user/emails";

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
pub struct GithubRedirectQuery {
    token: Option<String>,
}

#[derive(Deserialize)]
pub struct GithubCallbackQuery {
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

#[derive(Deserialize)]
struct GithubUserInfo {
    id:         i64,
    login:      String,
    name:       Option<String>,
    avatar_url: Option<String>,
}

#[derive(Deserialize)]
struct GithubEmail {
    email:    String,
    primary:  bool,
    verified: bool,
}

/// Google OAuth リダイレクト URL へ転送します。
pub async fn google_redirect(
    State(state): State<AppState>,
    Query(params): Query<GoogleRedirectQuery>,
) -> Redirect {
    let oauth_state = match params.token.as_deref().filter(|t| !t.is_empty()) {
        Some(token) => format!("{}|{}", state.cfg.app.runtime, token),
        None        => state.cfg.app.runtime.clone(),
    };
    let url = format!(
        "https://accounts.google.com/o/oauth2/auth?client_id={}&redirect_uri={}&response_type=code&scope=email+profile&access_type=online&state={}",
        state.cfg.oauth.google_client_id,
        percent_encoding::utf8_percent_encode(&state.cfg.oauth.google_redirect_url, percent_encoding::NON_ALPHANUMERIC),
        percent_encoding::utf8_percent_encode(&oauth_state, percent_encoding::NON_ALPHANUMERIC),
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
    let invitation_token = params.state.and_then(|s| {
        let parts: Vec<&str> = s.splitn(2, '|').collect();
        parts.get(1).map(|t| t.to_string())
    });

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

/// GitHub OAuth リダイレクト URL へ転送します。
pub async fn github_redirect(
    State(state): State<AppState>,
    Query(params): Query<GithubRedirectQuery>,
) -> Redirect {
    let oauth_state = match params.token.as_deref().filter(|t| !t.is_empty()) {
        Some(token) => format!("{}|{}", state.cfg.app.runtime, token),
        None        => state.cfg.app.runtime.clone(),
    };
    let url = format!(
        "{}?client_id={}&redirect_uri={}&scope=user:email&state={}",
        GITHUB_AUTH_URL,
        state.cfg.oauth.github_client_id,
        percent_encoding::utf8_percent_encode(&state.cfg.oauth.github_redirect_url, percent_encoding::NON_ALPHANUMERIC),
        percent_encoding::utf8_percent_encode(&oauth_state, percent_encoding::NON_ALPHANUMERIC),
    );
    Redirect::temporary(&url)
}

/// GitHub OAuth コールバックを処理してセッションを発行します。
pub async fn github_callback(
    State(state): State<AppState>,
    jar: CookieJar,
    Query(params): Query<GithubCallbackQuery>,
) -> impl IntoResponse {
    let cfg       = &state.cfg;
    let error_url = format!("{}/error?code=500", cfg.app.frontend_url);

    let code = match params.code.filter(|c| !c.is_empty()) {
        Some(c) => c,
        None    => return (jar, Redirect::temporary(&format!("{}/error?code=400", cfg.app.frontend_url))).into_response(),
    };
    let invitation_token = params.state.and_then(|s| {
        let parts: Vec<&str> = s.splitn(2, '|').collect();
        parts.get(1).map(|t| t.to_string())
    });

    let client = reqwest::Client::builder()
        .user_agent("authorization-app")
        .build()
        .unwrap_or_default();

    let token_resp = client
        .post(GITHUB_TOKEN_URL)
        .header("Accept", "application/json")
        .form(&[
            ("client_id",     cfg.oauth.github_client_id.as_str()),
            ("client_secret", cfg.oauth.github_client_secret.as_str()),
            ("redirect_uri",  cfg.oauth.github_redirect_url.as_str()),
            ("code",          code.as_str()),
        ])
        .send().await;

    let token: TokenResponse = match token_resp {
        Ok(r) => match r.json::<TokenResponse>().await {
            Ok(t)  => t,
            Err(e) => { tracing::error!("github token parse failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
        },
        Err(e) => { tracing::error!("github token exchange failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let user_resp = client
        .get(GITHUB_USER_URL)
        .bearer_auth(&token.access_token)
        .header("Accept", "application/json")
        .send().await;

    let github_user: GithubUserInfo = match user_resp {
        Ok(r) => match r.json::<GithubUserInfo>().await {
            Ok(u)  => u,
            Err(e) => { tracing::error!("github user parse failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
        },
        Err(e) => { tracing::error!("github user fetch failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let emails_resp = client
        .get(GITHUB_EMAILS_URL)
        .bearer_auth(&token.access_token)
        .header("Accept", "application/json")
        .send().await;

    let emails: Vec<GithubEmail> = match emails_resp {
        Ok(r) => match r.json::<Vec<GithubEmail>>().await {
            Ok(e)  => e,
            Err(e) => { tracing::error!("github emails parse failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
        },
        Err(e) => { tracing::error!("github emails fetch failed: {}", e); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let email = match emails.into_iter().find(|e| e.primary && e.verified) {
        Some(e) => e.email,
        None    => { tracing::error!("github primary email not found"); return (jar, Redirect::temporary(&error_url)).into_response(); }
    };

    let name = github_user.name.unwrap_or(github_user.login);

    let dto = LoginDto {
        provider:         2,
        provider_id:      github_user.id.to_string(),
        name,
        email,
        avatar:           github_user.avatar_url,
        invitation_token,
    };

    let vo = match state.auth_uc.login(dto).await {
        Ok(v)  => v,
        Err(e) => {
            let msg = e.to_string();
            if msg.contains("invitation_required") {
                return (jar, Redirect::temporary(&format!("{}/error?code=403", cfg.app.frontend_url))).into_response();
            }
            tracing::error!("github login failed: {}", e);
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
