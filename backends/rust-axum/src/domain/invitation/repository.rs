//! 招待ドメイン リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::value_objects::Vo;
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// 招待のリポジトリインターフェース。
#[async_trait]
pub trait Repository: Send + Sync {
    /// 有効な招待トークンの VO を返します。存在しない場合は None を返します。
    async fn get_current(&self) -> Result<Option<Vo>, DomainError>;
    /// 新しい招待トークンを発行して VO を返します。
    async fn issue(&self) -> Result<Vo, DomainError>;
    /// トークン文字列で招待 VO を返します。存在しない場合は None を返します。
    async fn find_by_token(&self, token: &str) -> Result<Option<Vo>, DomainError>;
}
