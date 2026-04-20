use super::value_objects::Vo;
use async_trait::async_trait;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

#[async_trait]
pub trait Repository: Send + Sync {
    async fn get_current(&self) -> Result<Option<Vo>, DomainError>;
    async fn issue(&self) -> Result<Vo, DomainError>;
    async fn find_by_token(&self, token: &str) -> Result<Option<Vo>, DomainError>;
}
