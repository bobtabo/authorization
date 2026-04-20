use std::sync::Arc;
use crate::domain::client::{entity::Client, repository::Repository};
use super::dto::{ListConditionDto, StoreDto, UpdateDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    pub async fn find_by_condition(&self, _dto: ListConditionDto) -> Result<Vec<Client>, UseCaseError> {
        todo!()
    }

    pub async fn find_by_id(&self, _id: u64) -> Result<Client, UseCaseError> {
        todo!()
    }

    pub async fn store(&self, _dto: StoreDto) -> Result<Client, UseCaseError> {
        todo!()
    }

    pub async fn update(&self, _dto: UpdateDto) -> Result<Client, UseCaseError> {
        todo!()
    }

    pub async fn destroy(&self, _id: u64, _executor_id: u32) -> Result<(), UseCaseError> {
        todo!()
    }

    pub async fn find_by_access_token(&self, _token: &str) -> Result<Option<Client>, UseCaseError> {
        todo!()
    }

    pub async fn find_by_identifier(&self, _identifier: &str) -> Result<Option<Client>, UseCaseError> {
        todo!()
    }
}
