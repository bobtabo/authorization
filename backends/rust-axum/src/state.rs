//! アプリケーション状態モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use sqlx::MySqlPool;
use crate::{
    config::Config,
    infrastructure::mail::Mailer,
    usecase,
};

/// Axum ハンドラーに共有される状態。
#[derive(Clone)]
pub struct AppState {
    pub cfg:             Arc<Config>,
    /// SQLx コネクションプール（ハンドラーのトランザクション処理に使用）。
    pub pool:            MySqlPool,
    pub auth_uc:         Arc<usecase::auth::Interactor>,
    pub client_uc:       Arc<usecase::client::Interactor>,
    pub staff_uc:        Arc<usecase::staff::Interactor>,
    pub invitation_uc:   Arc<usecase::invitation::Interactor>,
    pub gate_uc:         Arc<usecase::gate::Interactor>,
    pub notification_uc: Arc<usecase::notification::Interactor>,
    pub mailer:          Arc<Mailer>,
}
