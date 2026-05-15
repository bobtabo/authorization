//! ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

pub mod admin_invitation;
pub mod auth;
pub mod client;
pub mod gate;
pub mod mobile_client;
pub mod notification;
pub mod staff;

use axum_extra::extract::CookieJar;

pub const TIME_FORMAT: &str = "%Y-%m-%d %H:%M";

/// Cookie からスタッフ ID を取得します。未設定または不正値の場合は 0 を返します。
pub fn staff_id_from_cookie(jar: &CookieJar) -> u32 {
    jar.get("staff_id")
        .and_then(|c| c.value().parse::<u32>().ok())
        .unwrap_or(0)
}
