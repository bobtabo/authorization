//! 通知ドメイン リポジトリインターフェースモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use super::value_objects::EntityPage;
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

/// 通知のリポジトリインターフェース。
#[async_trait]
pub trait Repository: Send + Sync {
    /// カーソルページングで通知エンティティ一覧と次カーソルを返します。
    async fn list_page(&self, staff_id: u32, cursor: Option<&str>, limit: i32) -> Result<EntityPage, DomainError>;
    /// スタッフの未読・全件数をタプルで返します。
    async fn counts(&self, staff_id: u32) -> Result<(i64, i64), DomainError>;
    /// 対象通知を一括既読にして更新件数を返します。
    async fn bulk_mark_read(&self, staff_id: i64, ids: Vec<i64>, all: bool) -> Result<i64, DomainError>;
    /// 通知を新規登録します。
    async fn store(&self, staff_id: u32, message_type: i32, title: &str, message: &str, created_by: u32, url: Option<&str>) -> Result<(), DomainError>;
    /// 通知を部分更新して成否を返します。
    async fn patch(&self, id: i64, read: bool) -> Result<bool, DomainError>;
}
