//! クライアントドメイン リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::{condition::Condition, entity::{Client, JwtHistory}};
use async_trait::async_trait;
use chrono::{DateTime, Utc};

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

/// JWT 履歴リポジトリのインターフェース。
#[async_trait]
pub trait JwtHistoryRepository: Send + Sync {
    /// 指定したクライアント ID の JWT 履歴一覧を issue_at 降順で返します。
    async fn find_by_client_id(&self, client_id: u64) -> Result<Vec<JwtHistory>, DomainError>;
    /// JWT 履歴を保存します。
    async fn save(
        &self,
        client_id: u64,
        member_id: &str,
        issue_at: DateTime<Utc>,
        jwt: &str,
    ) -> Result<(), DomainError>;
}
