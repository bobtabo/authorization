//! スタッフドメイン リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::{condition::Condition, entity::Staff};
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// スタッフのリポジトリインターフェース。
#[async_trait]
pub trait Repository: Send + Sync {
    /// 検索条件に合致するスタッフ件数を返します。
    async fn count_by_condition(&self, cond: Condition) -> Result<i64, DomainError>;
    /// 検索条件に合致するスタッフ一覧を返します。
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Staff>, DomainError>;
    /// ID でスタッフを返します（論理削除済みは除く）。
    async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError>;
    /// OAuth プロバイダー情報でスタッフを返します。
    async fn find_by_provider(&self, provider: i32, provider_id: &str) -> Result<Option<Staff>, DomainError>;
    /// 論理削除されていない全スタッフを返します。
    async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError>;
    /// スタッフを登録または更新して返します。
    async fn save(&self, s: Staff) -> Result<Staff, DomainError>;
    /// スタッフのロールを更新して成否を返します。楽観排他エラー時は Err を返します。
    async fn update_role(&self, id: u32, role: i32, updated_by: u32, version: i32) -> Result<bool, DomainError>;
    /// スタッフを論理削除して成否を返します。楽観排他エラー時は Err を返します。
    async fn soft_delete(&self, id: u32, deleted_by: u32, version: i32) -> Result<bool, DomainError>;
    /// スタッフの論理削除を復元して成否を返します。
    async fn restore(&self, id: u32) -> Result<bool, DomainError>;
}
