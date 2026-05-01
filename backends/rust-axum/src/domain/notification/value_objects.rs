//! 通知ドメイン 値オブジェクトモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::entity::Notification;

/// 通知一覧アイテム VO（"YYYY-MM-DD HH:MM" 形式の日時文字列を持つ）。
pub struct NotificationItem {
    pub id:           u64,
    pub staff_id:     u32,
    pub message_type: i32,
    pub title:        String,
    pub message:      String,
    pub url:          Option<String>,
    pub read:         bool,
    pub created_at:   String,
    pub updated_at:   String,
}

/// カーソルページング通知一覧 VO（インタラクターが返す）。
pub struct Page {
    pub items:       Vec<NotificationItem>,
    pub next_cursor: Option<String>,
}

/// リポジトリが返すエンティティベースのページ。
pub struct EntityPage {
    pub items:       Vec<Notification>,
    pub next_cursor: Option<String>,
}
