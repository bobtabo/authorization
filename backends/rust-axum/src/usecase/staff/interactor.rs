use std::sync::Arc;
use crate::domain::staff::{entity::Staff, condition::Condition, repository::Repository};
use super::dto::{UpdateRoleDto, DestroyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    pub async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<Staff>, UseCaseError> {
        todo!()
    }

    pub async fn update_role(&self, _dto: UpdateRoleDto) -> Result<(), UseCaseError> {
        todo!()
    }

    pub async fn restore(&self, _id: u32) -> Result<(), UseCaseError> {
        todo!()
    }

    pub async fn destroy(&self, _dto: DestroyDto) -> Result<(), UseCaseError> {
        todo!()
    }

    pub fn status(s: &Staff) -> i32 {
        if s.deleted_at.is_some() { 0 } else { 1 }
    }
}
