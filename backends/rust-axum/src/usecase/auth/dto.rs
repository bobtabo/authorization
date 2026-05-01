//! 認証ユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// ログイン入力 DTO。
pub struct LoginDto {
    pub provider:         i32,
    pub provider_id:      String,
    pub name:             String,
    pub email:            String,
    pub avatar:           Option<String>,
    pub invitation_token: Option<String>,
}
