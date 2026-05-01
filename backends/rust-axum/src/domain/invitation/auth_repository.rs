//! 招待認証リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// 招待認証トークンのキャッシュリポジトリインターフェース。
#[async_trait]
pub trait AuthRepository: Send + Sync {
    /// トークンを指定秒数キャッシュします。
    async fn store(&self, token: &str, ttl: u64) -> Result<(), DomainError>;
    /// トークンを取得します。存在しない場合は None を返します。
    async fn find(&self, token: &str) -> Result<Option<String>, DomainError>;
    /// トークンを削除します。
    async fn remove(&self, token: &str) -> Result<(), DomainError>;
}
