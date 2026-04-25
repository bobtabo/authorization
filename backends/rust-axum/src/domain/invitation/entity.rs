//! 招待ドメイン エンティティモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::NaiveDateTime;

/// 招待のドメインエンティティ。
pub struct Invitation {
    pub id:         u32,
    pub token:      String,
    pub created_at: NaiveDateTime,
    pub created_by: Option<u32>,
    pub updated_at: NaiveDateTime,
    pub updated_by: Option<u32>,
    pub deleted_at: Option<NaiveDateTime>,
    pub deleted_by: Option<u32>,
    pub version:    i32,
}
