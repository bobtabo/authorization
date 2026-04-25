//! 通知ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::{
    notification::{value_objects::Page, repository::Repository},
    staff::repository::Repository as StaffRepository,
};
use super::dto::FanOutDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 通知のユースケース実装。
pub struct Interactor {
    repo:       Arc<dyn Repository>,
    staff_repo: Arc<dyn StaffRepository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>, staff_repo: Arc<dyn StaffRepository>) -> Self {
        Self { repo, staff_repo }
    }

    /// カーソルページングで通知 VO 一覧を返します。
    pub async fn list_page(
        &self,
        _staff_id: u32,
        _cursor: Option<String>,
        _limit: i64,
    ) -> Result<Page, UseCaseError> {
        todo!()
    }

    /// スタッフの未読・全件数をタプルで返します。
    pub async fn counts(&self, _staff_id: u32) -> Result<(i64, i64), UseCaseError> {
        todo!()
    }

    /// スタッフの通知を全件既読にして更新件数を返します。
    pub async fn bulk_mark_read(&self, _staff_id: u32) -> Result<i64, UseCaseError> {
        todo!()
    }

    /// 全スタッフに通知を一斉配信します。
    pub async fn fan_out(&self, _dto: FanOutDto) -> Result<(), UseCaseError> {
        todo!()
    }

    /// 通知を既読にします。
    pub async fn mark_read(&self, _id: i64) -> Result<(), UseCaseError> {
        todo!()
    }
}
