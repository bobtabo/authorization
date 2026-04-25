//! Gate ユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// JWT 発行入力 DTO。
pub struct IssueDto {
    pub access_token: String,
    pub member_id:    String,
}

/// JWT 検証入力 DTO。
pub struct VerifyDto {
    pub identifier: String,
    pub token:      String,
}
