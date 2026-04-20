use async_trait::async_trait;
use sqlx::MySqlPool;
use crate::domain::client::{
    condition::Condition,
    entity::Client,
    repository::{DomainError, Repository},
};

pub struct SqlxClientRepository {
    pool: MySqlPool,
}

impl SqlxClientRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository for SqlxClientRepository {
    async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<Client>, DomainError> {
        todo!()
    }
    async fn find_by_id(&self, _id: u64) -> Result<Option<Client>, DomainError> {
        todo!()
    }
    async fn find_by_access_token(&self, _token: &str) -> Result<Option<Client>, DomainError> {
        todo!()
    }
    async fn find_by_identifier(&self, _identifier: &str) -> Result<Option<Client>, DomainError> {
        todo!()
    }
    async fn save(&self, _c: Client) -> Result<Client, DomainError> {
        todo!()
    }
    async fn soft_delete(&self, _id: u64, _deleted_by: u32) -> Result<(), DomainError> {
        todo!()
    }
}
