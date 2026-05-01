//! Gate ドメイン 値オブジェクト・リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;

/// Gate JWT 発行結果 VO。
pub struct IssueVo {
    pub token: String,
}

/// Gate JWT 検証結果 VO。
pub struct VerifyVo {
    pub claims: serde_json::Value,
}

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// Gate JWT キャッシュのリポジトリインターフェース。
#[async_trait]
pub trait CacheRepository: Send + Sync {
    /// キャッシュから JWT を取得します。キャッシュミス時は None を返します。
    async fn get_jwt(&self, identifier: &str, member_id: &str) -> Result<Option<String>, DomainError>;
    /// JWT をキャッシュに保存します。
    async fn put_jwt(&self, identifier: &str, member_id: &str, token: &str, ttl: i64) -> Result<(), DomainError>;
}
