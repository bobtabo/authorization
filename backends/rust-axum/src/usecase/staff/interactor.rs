//! スタッフユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::staff::{
    condition::Condition,
    repository::Repository,
    value_objects::ListItem,
};
use super::dto::{UpdateRoleDto, DestroyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// スタッフのユースケース実装。
pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    /// 検索条件に合致するスタッフ一覧の VO を返します。
    pub async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<ListItem>, UseCaseError> {
        todo!()
    }

    /// スタッフのロールを更新します。
    pub async fn update_role(&self, _dto: UpdateRoleDto) -> Result<(), UseCaseError> {
        todo!()
    }

    /// スタッフの論理削除を復元します。
    pub async fn restore(&self, _id: u32) -> Result<(), UseCaseError> {
        todo!()
    }

    /// スタッフを論理削除します。
    pub async fn destroy(&self, _dto: DestroyDto) -> Result<(), UseCaseError> {
        todo!()
    }
}
