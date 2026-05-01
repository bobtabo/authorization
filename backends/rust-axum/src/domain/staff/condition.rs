//! スタッフドメイン 検索条件モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// スタッフ一覧の検索条件。
pub struct Condition {
    pub keyword: Option<String>,
    pub roles:   Vec<i32>,
}
