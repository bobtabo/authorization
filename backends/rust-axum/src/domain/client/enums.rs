//! クライアントドメイン 列挙定数モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// クライアントステータス: 非アクティブ。
pub const STATUS_INACTIVE:  i32 = 1;
/// クライアントステータス: 稼働中。
pub const STATUS_ACTIVE:    i32 = 2;
/// クライアントステータス: 停止中。
pub const STATUS_SUSPENDED: i32 = 3;
/// クライアントステータス: 閉鎖。
pub const STATUS_CLOSED:    i32 = 4;
