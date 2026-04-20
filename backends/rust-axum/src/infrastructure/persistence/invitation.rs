use async_trait::async_trait;
use sqlx::MySqlPool;
use crate::domain::invitation::{
    value_objects::Vo,
    repository::{DomainError, Repository},
};

pub struct SqlxInvitationRepository {
    pool:         MySqlPool,
    frontend_url: String,
}

impl SqlxInvitationRepository {
    pub fn new(pool: MySqlPool, frontend_url: String) -> Self {
        Self { pool, frontend_url }
    }
}

#[async_trait]
impl Repository for SqlxInvitationRepository {
    async fn get_current(&self) -> Result<Option<Vo>, DomainError> {
        todo!()
    }
    async fn issue(&self) -> Result<Vo, DomainError> {
        todo!()
    }
    async fn find_by_token(&self, _token: &str) -> Result<Option<Vo>, DomainError> {
        todo!()
    }
}
