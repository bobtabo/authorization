//! 認証ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::staff::{
    entity::Staff,
    enums::ROLE_MEMBER,
    repository::Repository,
    value_objects::Vo,
};
use crate::domain::invitation::auth_repository::AuthRepository as InvitationAuthRepository;
use super::dto::LoginDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 認証のユースケース実装。
pub struct Interactor {
    staff_repo:            Arc<dyn Repository>,
    invitation_auth_repo:  Arc<dyn InvitationAuthRepository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(staff_repo: Arc<dyn Repository>, invitation_auth_repo: Arc<dyn InvitationAuthRepository>) -> Self {
        Self { staff_repo, invitation_auth_repo }
    }

    /// スタッフ ID でログイン中スタッフの VO を返します。
    pub async fn find_user(&self, id: u32) -> Result<Vo, UseCaseError> {
        let s = self.staff_repo.find_by_id(id).await?
            .ok_or_else(|| -> UseCaseError { "user_not_found".to_string().into() })?;
        Ok(to_vo(s))
    }

    /// OAuth 情報でスタッフを upsert してログインし、VO を返します。
    pub async fn login(&self, dto: LoginDto) -> Result<Vo, UseCaseError> {
        let now = chrono::Utc::now();
        let existing = self.staff_repo.find_by_provider(dto.provider, &dto.provider_id).await?;

        let saved = if let Some(mut s) = existing {
            s.avatar        = dto.avatar;
            s.last_login_at = Some(now);
            s.updated_at    = now;
            self.staff_repo.save(s).await?
        } else {
            let token = dto.invitation_token.as_deref().unwrap_or("");
            if token.is_empty() || self.invitation_auth_repo.find(token).await?.is_none() {
                return Err("invitation_required".to_string().into());
            }
            self.invitation_auth_repo.remove(token).await?;
            let new_staff = Staff {
                id:            0,
                name:          dto.name,
                email:         dto.email,
                provider:      dto.provider,
                provider_id:   dto.provider_id,
                avatar:        dto.avatar,
                role:          ROLE_MEMBER,
                last_login_at: Some(now),
                created_at:    now,
                created_by:    Some(0),
                updated_at:    now,
                updated_by:    Some(0),
                deleted_at:    None,
                deleted_by:    None,
                version:       0,
            };
            self.staff_repo.save(new_staff).await?
        };

        Ok(to_vo(saved))
    }
}

fn to_vo(s: Staff) -> Vo {
    Vo {
        id:     s.id,
        name:   s.name,
        avatar: s.avatar,
        role:   s.role,
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
    use crate::domain::invitation::auth_repository::AuthRepository as InvAuthRepo;

    struct MockStaffRepo {
        find_by_provider: Mutex<Option<Option<Staff>>>,
        find_by_id:       Mutex<Option<Option<Staff>>>,
        saved:            Mutex<Option<Staff>>,
    }

    impl MockStaffRepo {
        fn new() -> Self {
            Self {
                find_by_provider: Mutex::new(None),
                find_by_id:       Mutex::new(None),
                saved:            Mutex::new(None),
            }
        }
    }

    struct MockInvitationAuthRepo {
        stored: Mutex<Option<String>>,
    }
    impl MockInvitationAuthRepo {
        fn with_token(token: &str) -> Self { Self { stored: Mutex::new(Some(token.to_string())) } }
        fn empty() -> Self { Self { stored: Mutex::new(None) } }
    }
    #[async_trait]
    impl InvAuthRepo for MockInvitationAuthRepo {
        async fn store(&self, _: &str, _: u64) -> Result<(), DomainError> { Ok(()) }
        async fn find(&self, _: &str) -> Result<Option<String>, DomainError> {
            Ok(self.stored.lock().unwrap().clone())
        }
        async fn remove(&self, _: &str) -> Result<(), DomainError> { Ok(()) }
    }

    fn make_staff(id: u32) -> Staff {
        let now = chrono::Utc::now();
        Staff {
            id,
            name:          "Alice".to_string(),
            email:         "alice@example.com".to_string(),
            provider:      1,
            provider_id:   "goog1".to_string(),
            avatar:        Some("http://example.com/avatar.png".to_string()),
            role:          2,
            last_login_at: None,
            created_at:    now,
            created_by:    None,
            updated_at:    now,
            updated_by:    None,
            deleted_at:    None,
            deleted_by:    None,
            version:       0,
        }
    }

    #[async_trait]
    impl Repository for MockStaffRepo {
        async fn find_by_condition(&self, _: Condition) -> Result<Vec<Staff>, DomainError> { Ok(vec![]) }
        async fn find_by_id(&self, id: u32) -> Result<Option<Staff>, DomainError> {
            Ok(self.find_by_id.lock().unwrap().take().unwrap_or(Some(make_staff(id))))
        }
        async fn find_by_provider(&self, _: i32, _: &str) -> Result<Option<Staff>, DomainError> {
            Ok(self.find_by_provider.lock().unwrap().take().unwrap_or(None))
        }
        async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError> { Ok(vec![]) }
        async fn save(&self, s: Staff) -> Result<Staff, DomainError> {
            let result = self.saved.lock().unwrap().take().unwrap_or(s);
            Ok(result)
        }
        async fn update_role(&self, _: u32, _: i32, _: u32, _: i32) -> Result<bool, DomainError> { Ok(true) }
        async fn soft_delete(&self, _: u32, _: u32, _: i32) -> Result<bool, DomainError> { Ok(true) }
        async fn restore(&self, _: u32) -> Result<bool, DomainError> { Ok(true) }
    }

    fn make_uc(staff: Arc<MockStaffRepo>, auth: Arc<dyn InvAuthRepo>) -> Interactor {
        Interactor::new(staff, auth)
    }

    #[tokio::test]
    async fn test_find_user_returns_vo() {
        let mock = Arc::new(MockStaffRepo::new());
        let uc = make_uc(mock, Arc::new(MockInvitationAuthRepo::empty()));
        let vo = uc.find_user(1).await.unwrap();
        assert_eq!(vo.id, 1);
        assert_eq!(vo.name, "Alice");
    }

    #[tokio::test]
    async fn test_find_user_not_found() {
        let mock = Arc::new(MockStaffRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(None);
        let uc = make_uc(mock, Arc::new(MockInvitationAuthRepo::empty()));
        let result = uc.find_user(99).await;
        assert!(result.is_err());
    }

    #[tokio::test]
    async fn test_login_creates_new_staff_with_invitation() {
        let mock = Arc::new(MockStaffRepo::new());
        *mock.find_by_provider.lock().unwrap() = Some(None);
        let uc = make_uc(mock, Arc::new(MockInvitationAuthRepo::with_token("tok")));
        let dto = LoginDto {
            provider:         1,
            provider_id:      "new_id".to_string(),
            name:             "Bob".to_string(),
            email:            "bob@example.com".to_string(),
            avatar:           None,
            invitation_token: Some("tok".to_string()),
        };
        let vo = uc.login(dto).await.unwrap();
        assert_eq!(vo.name, "Bob");
    }

    #[tokio::test]
    async fn test_login_new_staff_without_invitation_fails() {
        let mock = Arc::new(MockStaffRepo::new());
        *mock.find_by_provider.lock().unwrap() = Some(None);
        let uc = make_uc(mock, Arc::new(MockInvitationAuthRepo::empty()));
        let dto = LoginDto {
            provider:         1,
            provider_id:      "new_id".to_string(),
            name:             "Bob".to_string(),
            email:            "bob@example.com".to_string(),
            avatar:           None,
            invitation_token: None,
        };
        let result = uc.login(dto).await;
        assert!(result.is_err());
    }

    #[tokio::test]
    async fn test_login_updates_existing_staff() {
        let mock = Arc::new(MockStaffRepo::new());
        let existing = make_staff(10);
        *mock.find_by_provider.lock().unwrap() = Some(Some(existing));
        let saved = make_staff(10);
        *mock.saved.lock().unwrap() = Some(saved);
        let uc = make_uc(mock, Arc::new(MockInvitationAuthRepo::empty()));
        let dto = LoginDto {
            provider:         1,
            provider_id:      "goog1".to_string(),
            name:             "Alice Updated".to_string(),
            email:            "alice@example.com".to_string(),
            avatar:           Some("new_avatar.png".to_string()),
            invitation_token: None,
        };
        let vo = uc.login(dto).await.unwrap();
        assert_eq!(vo.id, 10);
    }
}
