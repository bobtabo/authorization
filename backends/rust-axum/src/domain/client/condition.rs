//! クライアントドメイン 検索条件モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// クライアント一覧の検索条件。
pub struct Condition {
    pub keyword:    Option<String>,
    pub start_from: Option<DateTime<Utc>>,
    pub start_to: Option<DateTime<Utc>>,
    pub statuses:   Vec<i32>,
}
