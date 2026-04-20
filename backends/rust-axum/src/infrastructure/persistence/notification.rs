use async_trait::async_trait;
use sqlx::MySqlPool;
use std::collections::HashMap;
use crate::domain::notification::{
    value_objects::Page,
    repository::{DomainError, Repository},
};

pub struct SqlxNotificationRepository {
    pool: MySqlPool,
}

impl SqlxNotificationRepository {
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository for SqlxNotificationRepository {
    async fn list_page(&self, _staff_id: u32, _cursor: Option<&str>, _limit: i32) -> Result<Page, DomainError> {
        todo!()
    }
    async fn counts(&self, _staff_id: u32) -> Result<(i64, i64), DomainError> {
        todo!()
    }
    async fn bulk_mark_read(&self, _staff_id: i64, _ids: Vec<i64>, _all: bool) -> Result<i64, DomainError> {
        todo!()
    }
    async fn store(&self, _staff_id: u32, _message_type: i32, _title: &str, _message: &str, _created_by: u32, _url: Option<&str>) -> Result<(), DomainError> {
        todo!()
    }
    async fn patch(&self, _id: i64, _attrs: HashMap<String, serde_json::Value>) -> Result<bool, DomainError> {
        todo!()
    }
}
