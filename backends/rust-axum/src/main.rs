//! アプリケーションエントリーポイント。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use authorization::{build_router, build_state, config::Config};
use std::{net::SocketAddr, sync::Arc};

#[tokio::main]
async fn main() {
    tracing_subscriber::fmt::init();

    let cfg = Arc::new(Config::load());
    let (state, _) = build_state(cfg).await;
    let app = build_router(state);

    let addr = SocketAddr::from(([0, 0, 0, 0], 8080));
    let listener = tokio::net::TcpListener::bind(addr).await.unwrap();
    tracing::info!("listening on {}", addr);
    axum::serve(listener, app).await.unwrap();
}
