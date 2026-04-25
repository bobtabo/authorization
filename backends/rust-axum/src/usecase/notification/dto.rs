//! 通知ユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// 通知ファンアウト入力 DTO。
pub struct FanOutDto {
    pub title:        String,
    pub message:      String,
    pub message_type: i32,
    pub executor_id:  u32,
    pub url:          String,
}
