use std::sync::Arc;
use crate::domain::invitation::{value_objects::Vo, repository::Repository};
use super::dto::FindByTokenDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    pub async fn current(&self) -> Result<Vo, UseCaseError> {
        todo!()
    }

    pub async fn issue(&self) -> Result<Vo, UseCaseError> {
        todo!()
    }

    pub async fn find_by_token(&self, _dto: FindByTokenDto) -> Result<Vo, UseCaseError> {
        todo!()
    }
}
