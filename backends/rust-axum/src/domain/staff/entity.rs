//! スタッフドメイン エンティティモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// スタッフのドメインエンティティ。
pub struct Staff {
    pub id:            u32,
    pub name:          String,
    pub email:         String,
    pub provider:      i32,
    pub provider_id:   String,
    pub avatar:        Option<String>,
    pub role:          i32,
    pub last_login_at: Option<DateTime<Utc>>,
    pub created_at: DateTime<Utc>,
    pub created_by:    Option<u32>,
    pub updated_at: DateTime<Utc>,
    pub updated_by:    Option<u32>,
    pub deleted_at: Option<DateTime<Utc>>,
    pub deleted_by:    Option<u32>,
    pub version:       i32,
}
