//! Authorization library crate.
//! Exposes app-building functions for both the binary and integration tests.

pub mod config;
pub mod domain;
pub mod handler;
pub mod infrastructure;
pub mod state;
pub mod usecase;

use axum::{Router, routing::{delete, get, patch, post, put}};
use std::sync::Arc;

use crate::state::AppState;

/// AppState から Axum Router を構築します。
pub fn build_router(state: AppState) -> Router {
    Router::new()
        .route("/auth/google/redirect", get(handler::auth::google_redirect))
        .route("/auth/google/callback", get(handler::auth::google_callback))
        .nest("/api", api_routes())
        .with_state(state)
}

/// 設定から AppState と MySqlPool を構築します。
pub async fn build_state(cfg: Arc<config::Config>) -> (AppState, sqlx::MySqlPool) {
    let pool         = infrastructure::db::new(&cfg).await.expect("db connect failed");
    let redis_client = infrastructure::cache::new(&cfg).expect("redis connect failed");

    let client_repo       = Arc::new(infrastructure::persistence::client::SqlxClientRepository::new(pool.clone()));
    let staff_repo        = Arc::new(infrastructure::persistence::staff::SqlxStaffRepository::new(pool.clone()));
    let invitation_repo   = Arc::new(infrastructure::persistence::invitation::SqlxInvitationRepository::new(
        pool.clone(),
        cfg.app.frontend_url.clone(),
    ));
    let notification_repo = Arc::new(infrastructure::persistence::notification::SqlxNotificationRepository::new(pool.clone()));
    let gate_cache            = Arc::new(infrastructure::cache::RedisGateRepository::new(redis_client.clone(), &cfg));
    let invitation_auth_cache = Arc::new(infrastructure::cache::RedisInvitationAuthRepository::new(redis_client, &cfg));

    let auth_uc         = Arc::new(usecase::auth::Interactor::new(staff_repo.clone(), invitation_auth_cache.clone()));
    let client_uc       = Arc::new(usecase::client::Interactor::new(client_repo.clone()));
    let staff_uc        = Arc::new(usecase::staff::Interactor::new(staff_repo.clone()));
    let invitation_uc   = Arc::new(usecase::invitation::Interactor::new(invitation_repo, invitation_auth_cache));
    let gate_uc         = Arc::new(usecase::gate::Interactor::new(client_repo.clone(), gate_cache, cfg.clone()));
    let notification_uc = Arc::new(usecase::notification::Interactor::new(notification_repo, staff_repo.clone()));
    let mailer          = Arc::new(infrastructure::mail::Mailer::new(cfg.mail.clone()));

    let app_state = AppState {
        cfg: cfg.clone(),
        pool: pool.clone(),
        auth_uc,
        client_uc,
        staff_uc,
        invitation_uc,
        gate_uc,
        notification_uc,
        mailer,
    };

    (app_state, pool)
}

fn api_routes() -> Router<AppState> {
    Router::new()
        .route("/auth/me",                get(handler::auth::get_my_profile))
        .route("/auth/login",             get(handler::auth::login))
        .route("/auth/logout",            get(handler::auth::logout))
        .route("/auth/invitation/{token}", get(handler::auth::invitation))
        .route("/clients",                get(handler::client::index))
        .route("/clients/store",          post(handler::client::store))
        .route("/clients/{id}/update",     put(handler::client::update))
        .route("/clients/{id}",            get(handler::client::show).delete(handler::client::destroy))
        .route("/staffs",                 get(handler::staff::index))
        .route("/staffs/{id}/updateRole",  patch(handler::staff::update_role))
        .route("/staffs/{id}/restore",     patch(handler::staff::restore))
        .route("/staffs/{id}/delete",      delete(handler::staff::destroy))
        .route("/admin/invitation",       get(handler::admin_invitation::index))
        .route("/admin/invitation/issue", get(handler::admin_invitation::issue))
        .route("/gate/issue",                      get(handler::gate::issue))
        .route("/gate/client/{identifier}/verify", get(handler::gate::verify))
        .route("/notifications/counts", get(handler::notification::counts))
        .route("/notifications",        get(handler::notification::index).patch(handler::notification::read_all))
        .route("/notifications/{id}",   patch(handler::notification::read))
}
