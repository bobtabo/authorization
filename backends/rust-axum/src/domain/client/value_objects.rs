//! クライアントドメイン 値オブジェクトモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use chrono::{DateTime, Utc};

/// クライアント一覧レスポンス用 VO。
pub struct ListItem {
    pub id:         u64,
    pub name:       String,
    pub status:     i32,
    pub start_at: Option<DateTime<Utc>>,
    pub stop_at: Option<DateTime<Utc>>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// クライアント詳細レスポンス用 VO。
pub struct DetailVo {
    pub id:          u64,
    pub name:        String,
    pub identifier:  String,
    pub post_code:   String,
    pub pref:        String,
    pub city:        String,
    pub address:     String,
    pub building:    String,
    pub tel:         String,
    pub email:       String,
    pub status:      i32,
    pub start_at: Option<DateTime<Utc>>,
    pub stop_at: Option<DateTime<Utc>>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

/// クライアント登録結果 VO。メール送信・通知配信に必要なフィールドを含む。
pub struct StoreResultVo {
    pub id:         u64,
    pub name:       String,
    pub identifier: String,
    pub email:      String,
    pub token:      String,
}
