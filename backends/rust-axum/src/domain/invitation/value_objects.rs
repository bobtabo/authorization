//! 招待ドメイン 値オブジェクトモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// 招待トークン取得・発行結果の VO。
pub struct Vo {
    pub token:       String,
    pub role:        u8,
    pub url:         String,
    pub display_url: String,
}
