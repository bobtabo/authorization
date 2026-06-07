//! 通知ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::{
    notification::{
        entity::Notification,
        value_objects::{NotificationItem, Page},
        repository::Repository,
    },
    staff::repository::Repository as StaffRepository,
};
use super::dto::FanOutDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 通知のユースケース実装。
pub struct Interactor {
    repo:       Arc<dyn Repository>,
    staff_repo: Arc<dyn StaffRepository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>, staff_repo: Arc<dyn StaffRepository>) -> Self {
        Self { repo, staff_repo }
    }

    /// カーソルページングで通知 VO 一覧を返します。
    pub async fn list_page(
        &self,
        staff_id: u32,
        cursor: Option<String>,
        limit: i64,
    ) -> Result<Page, UseCaseError> {
        let limit = limit.max(1).min(100) as i32;
        let entity_page = self.repo.list_page(staff_id, cursor.as_deref(), limit).await?;
        let items = entity_page.items.into_iter().map(to_item).collect();
        Ok(Page { items, next_cursor: entity_page.next_cursor })
    }

    /// スタッフの未読・全件数をタプルで返します。
    pub async fn counts(&self, staff_id: u32) -> Result<(i64, i64), UseCaseError> {
        Ok(self.repo.counts(staff_id).await?)
    }

    /// スタッフの通知を全件既読にして更新件数を返します。
    pub async fn bulk_mark_read(&self, staff_id: u32) -> Result<i64, UseCaseError> {
        Ok(self.repo.bulk_mark_read(staff_id as i64, vec![], true).await?)
    }

    /// 全スタッフに通知を一斉配信します。
    pub async fn fan_out(&self, dto: FanOutDto) -> Result<(), UseCaseError> {
        let staffs = self.staff_repo.find_all_active().await?;
        for s in staffs {
            let _ = self.repo.store(
                s.id,
                dto.message_type,
                &dto.title,
                &dto.message,
                dto.executor_id,
                Some(dto.url.as_str()),
            ).await;
        }
        Ok(())
    }

    /// 通知を既読にします。
    pub async fn mark_read(&self, id: i64) -> Result<(), UseCaseError> {
        self.repo.patch(id, true).await?;
        Ok(())
    }
}

fn to_item(n: Notification) -> NotificationItem {
    NotificationItem {
        id:           n.id,
        staff_id:     n.staff_id,
        message_type: n.message_type,
        title:        n.title,
        message:      n.message,
        url:          n.url,
        read:         n.read,
        created_at:   n.created_at.format("%Y-%m-%d %H:%M").to_string(),
        updated_at:   n.updated_at.format("%Y-%m-%d %H:%M").to_string(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use async_trait::async_trait;
    use chrono::TimeZone;
    use std::sync::Mutex;
    use crate::domain::{
        notification::{
            entity::Notification,
            value_objects::EntityPage,
            repository::{DomainError, Repository},
        },
        staff::{
            condition::Condition,
            entity::Staff,
            repository::{DomainError as StaffDomainError, Repository as StaffRepository},
        },
    };

    struct MockNotifRepo {
        list_page_result: Mutex<Option<EntityPage>>,
        counts_result:    Mutex<(i64, i64)>,
        bulk_mark_result: Mutex<i64>,
    }

    impl MockNotifRepo {
        fn new() -> Self {
            Self {
                list_page_result: Mutex::new(None),
                counts_result:    Mutex::new((3, 10)),
                bulk_mark_result: Mutex::new(5),
            }
        }
    }

    struct MockStaffRepo {
        active_staffs: Mutex<Vec<Staff>>,
    }

    impl MockStaffRepo {
        fn new() -> Self { Self { active_staffs: Mutex::new(vec![]) } }
    }

    fn make_notification(id: u64) -> Notification {
        let fixed = chrono::Utc.with_ymd_and_hms(2024, 1, 1, 12, 0, 0).unwrap();
        Notification {
            id,
            staff_id:     1,
            message_type: 1,
            title:        "Title".to_string(),
            message:      "Message".to_string(),
            url:          None,
            read:         false,
            created_at:   fixed,
            created_by:   None,
            updated_at:   fixed,
            updated_by:   None,
            deleted_at:   None,
            deleted_by:   None,
            version:      1,
        }
    }

    fn make_staff(id: u32) -> Staff {
        let now = chrono::Utc::now();
        Staff {
            id,
            name: "Staff".to_string(), email: "s@example.com".to_string(),
            provider: 1, provider_id: "p1".to_string(), avatar: None, role: 2,
            last_login_at: None, created_at: now, created_by: None,
            updated_at: now, updated_by: None, deleted_at: None, deleted_by: None, version: 0,
        }
    }

    #[async_trait]
    impl Repository for MockNotifRepo {
        async fn list_page(&self, _: u32, _: Option<&str>, _: i32) -> Result<EntityPage, DomainError> {
            Ok(self.list_page_result.lock().unwrap().take().unwrap_or(EntityPage { items: vec![], next_cursor: None }))
        }
        async fn counts(&self, _: u32) -> Result<(i64, i64), DomainError> {
            Ok(*self.counts_result.lock().unwrap())
        }
        async fn bulk_mark_read(&self, _: i64, _: Vec<i64>, _: bool) -> Result<i64, DomainError> {
            Ok(*self.bulk_mark_result.lock().unwrap())
        }
        async fn store(&self, _: u32, _: i32, _: &str, _: &str, _: u32, _: Option<&str>) -> Result<(), DomainError> { Ok(()) }
        async fn patch(&self, _: i64, _: bool) -> Result<bool, DomainError> { Ok(true) }
    }

    #[async_trait]
    impl StaffRepository for MockStaffRepo {
        async fn count_by_condition(&self, _: Condition) -> Result<i64, StaffDomainError> { Ok(0) }
        async fn find_by_condition(&self, _: Condition) -> Result<Vec<Staff>, StaffDomainError> { Ok(vec![]) }
        async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, StaffDomainError> { Ok(Some(make_staff(id))) }
        async fn find_by_provider(&self, _: i32, _: &str) -> Result<Option<Staff>, StaffDomainError> { Ok(None) }
        async fn find_all_active(&self) -> Result<Vec<Staff>, StaffDomainError> {
            Ok(self.active_staffs.lock().unwrap().drain(..).collect())
        }
        async fn save(&self, s: Staff) -> Result<Staff, StaffDomainError> { Ok(s) }
        async fn update_role(&self, _: u32, _: i32, _: u32, _: i32) -> Result<bool, StaffDomainError> { Ok(true) }
        async fn soft_delete(&self, _: u32, _: u32, _: i32) -> Result<bool, StaffDomainError> { Ok(true) }
        async fn restore(&self, _: u32) -> Result<bool, StaffDomainError> { Ok(true) }
    }

    #[tokio::test]
    async fn test_list_page_maps_to_notification_items() {
        let notif_repo = Arc::new(MockNotifRepo::new());
        *notif_repo.list_page_result.lock().unwrap() = Some(EntityPage {
            items:       vec![make_notification(1), make_notification(2)],
            next_cursor: Some("cursor_abc".to_string()),
        });
        let staff_repo = Arc::new(MockStaffRepo::new());
        let uc = Interactor::new(notif_repo, staff_repo);
        let page = uc.list_page(1, None, 10).await.unwrap();
        assert_eq!(page.items.len(), 2);
        assert_eq!(page.next_cursor, Some("cursor_abc".to_string()));
        assert_eq!(page.items[0].created_at, "2024-01-01 12:00");
    }

    #[tokio::test]
    async fn test_counts_returns_tuple() {
        let notif_repo = Arc::new(MockNotifRepo::new());
        *notif_repo.counts_result.lock().unwrap() = (5, 20);
        let staff_repo = Arc::new(MockStaffRepo::new());
        let uc = Interactor::new(notif_repo, staff_repo);
        let (unread, total) = uc.counts(1).await.unwrap();
        assert_eq!(unread, 5);
        assert_eq!(total, 20);
    }

    #[tokio::test]
    async fn test_bulk_mark_read_returns_count() {
        let notif_repo = Arc::new(MockNotifRepo::new());
        *notif_repo.bulk_mark_result.lock().unwrap() = 7;
        let staff_repo = Arc::new(MockStaffRepo::new());
        let uc = Interactor::new(notif_repo, staff_repo);
        let count = uc.bulk_mark_read(1).await.unwrap();
        assert_eq!(count, 7);
    }

    #[tokio::test]
    async fn test_fan_out_stores_per_staff() {
        let notif_repo = Arc::new(MockNotifRepo::new());
        let staff_repo = Arc::new(MockStaffRepo::new());
        *staff_repo.active_staffs.lock().unwrap() = vec![make_staff(1), make_staff(2)];
        let uc = Interactor::new(notif_repo, staff_repo);
        let dto = FanOutDto {
            title:        "Title".to_string(),
            message:      "Msg".to_string(),
            message_type: 1,
            executor_id:  99,
            url:          "/clients/show?id=1".to_string(),
        };
        assert!(uc.fan_out(dto).await.is_ok());
    }

    #[tokio::test]
    async fn test_mark_read_succeeds() {
        let notif_repo = Arc::new(MockNotifRepo::new());
        let staff_repo = Arc::new(MockStaffRepo::new());
        let uc = Interactor::new(notif_repo, staff_repo);
        assert!(uc.mark_read(1).await.is_ok());
    }
}
