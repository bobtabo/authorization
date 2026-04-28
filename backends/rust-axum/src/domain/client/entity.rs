//! クライアントドメイン エンティティモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// クライアントのドメインエンティティ。
pub struct Client {
    pub id:           u64,
    pub name:         String,
    pub identifier:   String,
    pub post_code:    String,
    pub pref:         String,
    pub city:         String,
    pub address:      String,
    pub building:     String,
    pub tel:          String,
    pub email:        String,
    pub access_token: String,
    pub private_key:  String,
    pub public_key:   String,
    pub fingerprint:  String,
    pub status:       i32,
    pub start_at: Option<DateTime<Utc>>,
    pub stop_at: Option<DateTime<Utc>>,
    pub created_at: DateTime<Utc>,
    pub created_by:   Option<u32>,
    pub updated_at: DateTime<Utc>,
    pub updated_by:   Option<u32>,
    pub deleted_at: Option<DateTime<Utc>>,
    pub deleted_by:   Option<u32>,
    pub version:      i32,
}
