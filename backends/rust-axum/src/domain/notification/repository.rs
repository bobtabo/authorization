use super::value_objects::Page;
use async_trait::async_trait;
use std::collections::HashMap;

pub type DomainError = Box<dyn std::error::Error + Send + Sync>;

#[async_trait]
pub trait Repository: Send + Sync {
    async fn list_page(&self, staff_id: u32, cursor: Option<&str>, limit: i32) -> Result<Page, DomainError>;
    async fn counts(&self, staff_id: u32) -> Result<(i64, i64), DomainError>;
    async fn bulk_mark_read(&self, staff_id: i64, ids: Vec<i64>, all: bool) -> Result<i64, DomainError>;
    async fn store(&self, staff_id: u32, message_type: i32, title: &str, message: &str, created_by: u32, url: Option<&str>) -> Result<(), DomainError>;
    async fn patch(&self, id: i64, attrs: HashMap<String, serde_json::Value>) -> Result<bool, DomainError>;
}
