use std::sync::Arc;
use crate::domain::staff::{entity::Staff, repository::Repository};
use super::dto::LoginDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    staff_repo: Arc<dyn Repository>,
}

impl Interactor {
    pub fn new(staff_repo: Arc<dyn Repository>) -> Self {
        Self { staff_repo }
    }

    pub async fn find_user(&self, _id: u32) -> Result<Staff, UseCaseError> {
        todo!()
    }

    pub async fn login(&self, _dto: LoginDto) -> Result<Staff, UseCaseError> {
        todo!()
    }
}
