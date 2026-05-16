//! 招待ドメイン エンティティモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// 招待のドメインエンティティ。
pub struct Invitation {
    pub id:         u32,
    pub token:      String,
    pub role:       u8,
    pub created_at: DateTime<Utc>,
    pub created_by: Option<u32>,
    pub updated_at: DateTime<Utc>,
    pub updated_by: Option<u32>,
    pub deleted_at: Option<DateTime<Utc>>,
    pub deleted_by: Option<u32>,
    pub version:    i32,
}
