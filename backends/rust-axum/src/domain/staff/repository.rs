use super::{condition::Condition, entity::Staff};
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

#[async_trait]
pub trait Repository: Send + Sync {
    async fn find_by_condition(&self, cond: Condition) -> Result<Vec<Staff>, DomainError>;
    async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError>;
    async fn find_by_provider(&self, provider: i32, provider_id: &str) -> Result<Option<Staff>, DomainError>;
    async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError>;
    async fn save(&self, s: Staff) -> Result<Staff, DomainError>;
    async fn update_role(&self, id: u32, role: i32, updated_by: u32) -> Result<bool, DomainError>;
    async fn soft_delete(&self, id: u32, deleted_by: u32) -> Result<bool, DomainError>;
    async fn restore(&self, id: u32) -> Result<bool, DomainError>;
}
