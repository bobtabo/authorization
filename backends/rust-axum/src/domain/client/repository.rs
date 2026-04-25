//! クライアントドメイン リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::{condition::Condition, entity::Client};
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// クライアントのリポジトリインターフェース。
#[async_trait]
pub trait Repository: Send + Sync {
    /// 検索条件に合致するクライアント一覧を返します。
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Client>, DomainError>;
    /// ID でクライアントを返します。存在しない場合は None を返します。
    async fn find_by_id(&self, id: u64) -> Result<Option<Client>, DomainError>;
    /// アクセストークンでクライアントを返します。
    async fn find_by_access_token(&self, token: &str) -> Result<Option<Client>, DomainError>;
    /// 識別子でクライアントを返します。
    async fn find_by_identifier(&self, identifier: &str) -> Result<Option<Client>, DomainError>;
    /// クライアントを登録または更新して返します。
    async fn save(&self, c: Client) -> Result<Client, DomainError>;
    /// クライアントを論理削除します。
    async fn soft_delete(&self, id: u64, deleted_by: u32) -> Result<(), DomainError>;
}
