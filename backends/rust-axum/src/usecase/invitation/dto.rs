//! 招待ユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// 招待トークン検索入力 DTO。
pub struct FindByTokenDto {
    pub token: String,
}

/// 招待 current/issue 入力 DTO。
pub struct RoleDto {
    pub role: u8,
}
