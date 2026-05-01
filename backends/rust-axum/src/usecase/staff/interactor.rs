//! スタッフユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::staff::{
    condition::Condition,
    entity::Staff,
    repository::Repository,
    value_objects::ListItem,
};
use super::dto::{UpdateRoleDto, DestroyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// スタッフのユースケース実装。
pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    /// 検索条件に合致するスタッフ一覧の VO を返します。
    pub async fn find_by_condition(&self, cond: Condition) -> Result<Vec<ListItem>, UseCaseError> {
        let staffs = self.repo.find_by_condition(cond).await?;
        Ok(staffs.into_iter().map(to_list_item).collect())
    }

    /// スタッフのロールを更新します。
    pub async fn update_role(&self, dto: UpdateRoleDto) -> Result<(), UseCaseError> {
        let ok = self.repo.update_role(dto.id, dto.role, dto.executor_id).await?;
        if !ok {
            return Err("staff_not_found".to_string().into());
        }
        Ok(())
    }

    /// スタッフの論理削除を復元します。
    pub async fn restore(&self, id: u32) -> Result<(), UseCaseError> {
        let ok = self.repo.restore(id).await?;
        if !ok {
            return Err("staff_not_found".to_string().into());
        }
        Ok(())
    }

    /// スタッフを論理削除します。
    pub async fn destroy(&self, dto: DestroyDto) -> Result<(), UseCaseError> {
        let ok = self.repo.soft_delete(dto.id, dto.executor_id).await?;
        if !ok {
            return Err("staff_not_found".to_string().into());
        }
        Ok(())
    }
}

fn to_list_item(s: Staff) -> ListItem {
    let status = if s.deleted_at.is_some() { 0 } else { 1 };
    ListItem {
        id:         s.id,
        name:       s.name,
        email:      s.email,
        role:       s.role,
        status,
        created_at: s.created_at,
        updated_at: s.updated_at,
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use async_trait::async_trait;
    use std::sync::Mutex;
    use crate::domain::staff::{
        condition::Condition,
        entity::Staff,
        repository::{DomainError, Repository},
    };

    struct MockRepo {
        find_by_condition: Mutex<Option<Vec<Staff>>>,
        update_role_ok:    Mutex<bool>,
        restore_ok:        Mutex<bool>,
        soft_delete_ok:    Mutex<bool>,
    }

    impl MockRepo {
        fn new() -> Self {
            Self {
                find_by_condition: Mutex::new(None),
                update_role_ok:    Mutex::new(true),
                restore_ok:        Mutex::new(true),
                soft_delete_ok:    Mutex::new(true),
            }
        }
    }

    fn make_staff(id: u32, deleted: bool) -> Staff {
        let now = chrono::Utc::now();
        Staff {
            id,
            name:          "Test Staff".to_string(),
            email:         "staff@example.com".to_string(),
            provider:      1,
            provider_id:   "google123".to_string(),
            avatar:        None,
            role:          2,
            last_login_at: None,
            created_at:    now,
            created_by:    None,
            updated_at:    now,
            updated_by:    None,
            deleted_at:    if deleted { Some(now) } else { None },
            deleted_by:    None,
            version:       0,
        }
    }

    #[async_trait]
    impl Repository for MockRepo {
        async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<Staff>, DomainError> {
            Ok(self.find_by_condition.lock().unwrap().take().unwrap_or_default())
        }
        async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError> {
            Ok(Some(make_staff(id, false)))
        }
        async fn find_by_provider(&self, _: i32, _: &str) -> Result<Option<Staff>, DomainError> {
            Ok(None)
        }
        async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError> {
            Ok(vec![])
        }
        async fn save(&self, s: Staff) -> Result<Staff, DomainError> {
            Ok(s)
        }
        async fn update_role(&self, _: u32, _: i32, _: u32) -> Result<bool, DomainError> {
            Ok(*self.update_role_ok.lock().unwrap())
        }
        async fn soft_delete(&self, _: u32, _: u32) -> Result<bool, DomainError> {
            Ok(*self.soft_delete_ok.lock().unwrap())
        }
        async fn restore(&self, _: u32) -> Result<bool, DomainError> {
            Ok(*self.restore_ok.lock().unwrap())
        }
    }

    #[tokio::test]
    async fn test_find_by_condition_maps_status() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_condition.lock().unwrap() = Some(vec![
            make_staff(1, false),
            make_staff(2, true),
        ]);
        let uc = Interactor::new(mock);
        let result = uc.find_by_condition(Condition { keyword: None, roles: vec![] }).await.unwrap();
        assert_eq!(result.len(), 2);
        assert_eq!(result[0].status, 1);
        assert_eq!(result[1].status, 0);
    }

    #[tokio::test]
    async fn test_update_role_success() {
        let mock = Arc::new(MockRepo::new());
        let uc = Interactor::new(mock);
        let dto = UpdateRoleDto { id: 1, role: 1, executor_id: 99 };
        assert!(uc.update_role(dto).await.is_ok());
    }

    #[tokio::test]
    async fn test_update_role_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.update_role_ok.lock().unwrap() = false;
        let uc = Interactor::new(mock);
        let dto = UpdateRoleDto { id: 999, role: 1, executor_id: 99 };
        assert!(uc.update_role(dto).await.is_err());
    }

    #[tokio::test]
    async fn test_destroy_success() {
        let mock = Arc::new(MockRepo::new());
        let uc = Interactor::new(mock);
        let dto = DestroyDto { id: 1, executor_id: 99 };
        assert!(uc.destroy(dto).await.is_ok());
    }

    #[tokio::test]
    async fn test_restore_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.restore_ok.lock().unwrap() = false;
        let uc = Interactor::new(mock);
        assert!(uc.restore(999).await.is_err());
    }
}
