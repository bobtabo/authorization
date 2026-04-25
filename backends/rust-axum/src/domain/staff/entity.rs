//! スタッフドメイン エンティティモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::NaiveDateTime;

/// スタッフのドメインエンティティ。
pub struct Staff {
    pub id:            u32,
    pub name:          String,
    pub email:         String,
    pub provider:      i32,
    pub provider_id:   String,
    pub avatar:        Option<String>,
    pub role:          i32,
    pub last_login_at: Option<NaiveDateTime>,
    pub created_at:    NaiveDateTime,
    pub created_by:    Option<u32>,
    pub updated_at:    NaiveDateTime,
    pub updated_by:    Option<u32>,
    pub deleted_at:    Option<NaiveDateTime>,
    pub deleted_by:    Option<u32>,
    pub version:       i32,
}
