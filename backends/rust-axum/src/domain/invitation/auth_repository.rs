//! 招待認証リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// 招待認証トークンのキャッシュリポジトリインターフェース。
#[async_trait]
pub trait AuthRepository: Send + Sync {
    /// トークンとロールを指定秒数キャッシュします。
    async fn store(&self, token: &str, role: u8, ttl: u64) -> Result<(), DomainError>;
    /// トークンに紐づくロールを取得します。存在しない場合は None を返します。
    async fn find(&self, token: &str) -> Result<Option<u8>, DomainError>;
    /// トークンを削除します。
    async fn remove(&self, token: &str) -> Result<(), DomainError>;
}
