//! Gate ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::{
    config::Config,
    domain::{
        client::repository::Repository as ClientRepository,
        gate::value_objects::{CacheRepository, IssueVo, VerifyVo},
    },
};
use super::dto::{IssueDto, VerifyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// Gate JWT 発行・検証のユースケース実装。
pub struct Interactor {
    client_repo: Arc<dyn ClientRepository>,
    cache:       Arc<dyn CacheRepository>,
    cfg:         Arc<Config>,
}

impl Interactor {
    /// リポジトリ・キャッシュ・設定を受け取りインタラクターを生成します。
    pub fn new(
        client_repo: Arc<dyn ClientRepository>,
        cache: Arc<dyn CacheRepository>,
        cfg: Arc<Config>,
    ) -> Self {
        Self { client_repo, cache, cfg }
    }

    /// アクセストークンを検証し JWT を発行して VO を返します。
    pub async fn issue_token(&self, _dto: IssueDto) -> Result<IssueVo, UseCaseError> {
        todo!()
    }

    /// JWT を検証してクレームを含む VO を返します。
    pub async fn verify(&self, _dto: VerifyDto) -> Result<VerifyVo, UseCaseError> {
        todo!()
    }
}
