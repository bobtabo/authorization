//! スタッフドメイン 値オブジェクトモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// スタッフ一覧レスポンス用 VO。
pub struct ListItem {
    pub id:         u32,
    pub name:       String,
    pub email:      String,
    pub role:       i32,
    /// 0=削除済み, 1=有効。
    pub status:     i32,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// ログイン中スタッフ情報レスポンス用 VO。
pub struct Vo {
    pub id:     u32,
    pub name:   String,
    pub avatar: Option<String>,
    pub role:   i32,
}
