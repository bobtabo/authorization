use std::sync::Arc;
use crate::domain::{
    notification::{entity::Notification, value_objects::Page, repository::Repository},
    staff::repository::Repository as StaffRepository,
};
use super::dto::FanOutDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

pub struct Interactor {
    repo:       Arc<dyn Repository>,
    staff_repo: Arc<dyn StaffRepository>,
}

impl Interactor {
    pub fn new(repo: Arc<dyn Repository>, staff_repo: Arc<dyn StaffRepository>) -> Self {
        Self { repo, staff_repo }
    }

    pub async fn list_page(
        &self,
        _staff_id: u32,
        _cursor: Option<String>,
        _limit: i64,
    ) -> Result<Page, UseCaseError> {
        todo!()
    }

    pub async fn counts(&self, _staff_id: u32) -> Result<(i64, i64), UseCaseError> {
        todo!()
    }

    pub async fn bulk_mark_read(&self, _staff_id: u32) -> Result<i64, UseCaseError> {
        todo!()
    }

    pub async fn fan_out(&self, _dto: FanOutDto) -> Result<(), UseCaseError> {
        todo!()
    }

    pub async fn mark_read(&self, _id: i64) -> Result<(), UseCaseError> {
        todo!()
    }

    pub fn map_notification(n: &Notification) -> serde_json::Value {
        serde_json::json!({
            "id":           n.id,
            "staff_id":     n.staff_id,
            "message_type": n.message_type,
            "title":        n.title,
            "message":      n.message,
            "url":          n.url,
            "read":         n.read,
            "created_at":   n.created_at.format("%Y-%m-%d %H:%M").to_string(),
            "updated_at":   n.updated_at.format("%Y-%m-%d %H:%M").to_string(),
        })
    }
}
