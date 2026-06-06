//! スタッフドメイン 検索条件モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// スタッフ一覧の検索条件。
#[derive(Clone)]
pub struct Condition {
    pub keyword:   Option<String>,
    pub roles:     Vec<i32>,
    pub offset:    i64,
    pub limit:     i64,
    pub sort:      Option<String>,
    pub sort_type: Option<String>,
}
