//! データベース接続モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use crate::config::Config;
use sqlx::mysql::MySqlPool;

/// 設定からコネクションプールを生成します。
pub async fn new(cfg: &Config) -> Result<MySqlPool, sqlx::Error> {
    MySqlPool::connect(&cfg.db.dsn).await
}
