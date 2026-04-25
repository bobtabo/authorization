//! クライアントユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::client::{
    entity::Client,
    repository::Repository,
    value_objects::{DetailVo, ListItem, StoreResultVo},
};
use super::dto::{ListConditionDto, StoreDto, UpdateDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// クライアントのユースケース実装。
pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    /// 検索条件に合致するクライアント一覧の VO を返します。
    pub async fn find_by_condition(&self, _dto: ListConditionDto) -> Result<Vec<ListItem>, UseCaseError> {
        todo!()
    }

    /// ID でクライアント詳細の VO を返します。存在しない場合はエラーを返します。
    pub async fn find_by_id(&self, _id: u64) -> Result<DetailVo, UseCaseError> {
        todo!()
    }

    /// クライアントを新規登録し、登録結果の VO を返します。
    pub async fn store(&self, _dto: StoreDto) -> Result<StoreResultVo, UseCaseError> {
        todo!()
    }

    /// クライアントを更新し、更新後の詳細 VO を返します。
    pub async fn update(&self, _dto: UpdateDto) -> Result<DetailVo, UseCaseError> {
        todo!()
    }

    /// クライアントを論理削除します。
    pub async fn destroy(&self, _id: u64, _executor_id: u32) -> Result<(), UseCaseError> {
        todo!()
    }

    /// Bearer トークンでクライアントを認証します。認証成功の場合 Some(Client) を返します。
    pub async fn find_by_access_token(&self, _token: &str) -> Result<Option<Client>, UseCaseError> {
        todo!()
    }

    /// 識別子でクライアントを返します。存在しない場合は None を返します。
    pub async fn find_by_identifier(&self, _identifier: &str) -> Result<Option<Client>, UseCaseError> {
        todo!()
    }
}
