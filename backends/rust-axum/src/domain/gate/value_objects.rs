use async_trait::async_trait;

pub struct IssueVo {
    pub token: String,
}

pub struct VerifyVo {
    pub claims: serde_json::Value,
}

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

#[async_trait]
pub trait CacheRepository: Send + Sync {
    async fn get_jwt(&self, identifier: &str, member_id: &str) -> Result<Option<String>, DomainError>;
    async fn put_jwt(&self, identifier: &str, member_id: &str, token: &str, ttl: i64) -> Result<(), DomainError>;
}
