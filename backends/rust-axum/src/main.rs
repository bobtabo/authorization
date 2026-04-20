use axum::{Router, routing::{delete, get, patch, post, put}};
use std::net::SocketAddr;

mod config;
mod handler;
mod infrastructure;
mod usecase;

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt::init();

    let app = Router::new()
        // OAuth（ブラウザリダイレクトのため /api 外）
        .route("/auth/google/redirect", get(handler::auth::google_redirect))
        .route("/auth/google/callback", get(handler::auth::google_callback))
        .nest("/api", api_routes());

    let addr = SocketAddr::from(([0, 0, 0, 0], 8080));
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    tracing::info!("listening on {}", addr);
    axum::serve(listener, app).await.unwrap();
}

fn api_routes() -> Router {
    Router::new()
        // --- auth ---
        .route("/auth/me", get(handler::auth::get_my_profile))
        .route("/auth/login", get(handler::auth::login))
        .route("/auth/logout", get(handler::auth::logout))
        .route("/auth/invitation/:token", get(handler::auth::invitation))
        // --- clients ---
        .route("/clients", get(handler::client::index))
        .route("/clients/store", post(handler::client::store))
        .route("/clients/:id/update", put(handler::client::update))
        .route("/clients/:id", get(handler::client::show).delete(handler::client::destroy))
        // --- staffs ---
        .route("/staffs", get(handler::staff::index))
        .route("/staffs/:id/updateRole", patch(handler::staff::update_role))
        .route("/staffs/:id/restore", patch(handler::staff::restore))
        .route("/staffs/:id/delete", delete(handler::staff::destroy))
        // --- admin ---
        .route("/admin/invitation", get(handler::admin_invitation::index))
        .route("/admin/invitation/issue", get(handler::admin_invitation::issue))
        // --- gate ---
        .route("/gate/issue", get(handler::gate::issue))
        .route("/gate/client/:identifier/verify", get(handler::gate::verify))
        // --- notifications ---
        .route("/notifications/counts", get(handler::notification::counts))
        .route("/notifications", get(handler::notification::index).patch(handler::notification::read_all))
        .route("/notifications/:id", patch(handler::notification::read))
}
