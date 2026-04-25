//! クライアントドメイン 検索条件モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::NaiveDateTime;

/// クライアント一覧の検索条件。
pub struct Condition {
    pub keyword:    Option<String>,
    pub start_from: Option<NaiveDateTime>,
    pub start_to:   Option<NaiveDateTime>,
    pub statuses:   Vec<i32>,
}
