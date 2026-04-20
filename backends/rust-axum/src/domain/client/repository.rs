use super::{condition::Condition, entity::Client};
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

#[async_trait]
pub trait Repository: Send + Sync {
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Client>, DomainError>;
    async fn find_by_id(&self, id: u64) -> Result<Option<Client>, DomainError>;
    async fn find_by_access_token(&self, token: &str) -> Result<Option<Client>, DomainError>;
    async fn find_by_identifier(&self, identifier: &str) -> Result<Option<Client>, DomainError>;
    async fn save(&self, c: Client) -> Result<Client, DomainError>;
    async fn soft_delete(&self, id: u64, deleted_by: u32) -> Result<(), DomainError>;
}
