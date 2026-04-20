use std::sync::Arc;
use crate::{
    config::Config,
    domain::{
        client::repository::Repository as ClientRepository,
        gate::value_objects::CacheRepository,
    },
};
use super::dto::{IssueDto, VerifyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    client_repo: Arc<dyn ClientRepository>,
    cache:       Arc<dyn CacheRepository>,
    cfg:         Arc<Config>,
}

impl Interactor {
    pub fn new(
        client_repo: Arc<dyn ClientRepository>,
        cache: Arc<dyn CacheRepository>,
        cfg: Arc<Config>,
    ) -> Self {
        Self { client_repo, cache, cfg }
    }

    pub async fn issue_token(&self, _dto: IssueDto) -> Result<String, UseCaseError> {
        todo!()
    }

    pub async fn verify(&self, _dto: VerifyDto) -> Result<serde_json::Value, UseCaseError> {
        todo!()
    }
}
