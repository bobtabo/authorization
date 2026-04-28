//! 招待ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::invitation::{
    value_objects::Vo,
    repository::Repository,
    auth_repository::AuthRepository,
};
use super::dto::FindByTokenDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 招待のユースケース実装。
pub struct Interactor {
    repo:      Arc<dyn Repository>,
    auth_repo: Arc<dyn AuthRepository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>, auth_repo: Arc<dyn AuthRepository>) -> Self {
        Self { repo, auth_repo }
    }

    /// 現在有効な招待トークンの VO を返します。存在しない場合はエラーを返します。
    pub async fn current(&self) -> Result<Vo, UseCaseError> {
        self.repo.get_current().await?
            .ok_or_else(|| -> UseCaseError { "invitation_not_found".to_string().into() })
    }

    /// 新しい招待トークンを発行して VO を返します。
    pub async fn issue(&self) -> Result<Vo, UseCaseError> {
        Ok(self.repo.issue().await?)
    }

    /// トークン文字列で招待 VO を返します。無効な場合はエラーを返します。
    pub async fn find_by_token(&self, dto: FindByTokenDto) -> Result<Vo, UseCaseError> {
        let vo = self.repo.find_by_token(&dto.token).await?
            .ok_or_else(|| -> UseCaseError { "invitation_not_found".to_string().into() })?;
        self.auth_repo.store(&vo.token, 600).await?;
        Ok(vo)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use async_trait::async_trait;
    use std::sync::Mutex;
    use crate::domain::invitation::{
        repository::{DomainError, Repository},
        auth_repository::AuthRepository,
    };

    struct MockRepo {
        current:  Mutex<Option<Option<Vo>>>,
        issue:    Mutex<Option<Vo>>,
        by_token: Mutex<Option<Option<Vo>>>,
    }

    impl MockRepo {
        fn new() -> Self {
            Self {
                current:  Mutex::new(None),
                issue:    Mutex::new(None),
                by_token: Mutex::new(None),
            }
        }
    }

    struct MockAuthRepo;

    #[async_trait]
    impl AuthRepository for MockAuthRepo {
        async fn store(&self, _: &str, _: u64) -> Result<(), DomainError> { Ok(()) }
        async fn find(&self, _: &str) -> Result<Option<String>, DomainError> { Ok(None) }
        async fn remove(&self, _: &str) -> Result<(), DomainError> { Ok(()) }
    }

    fn make_vo() -> Vo {
        Vo {
            token:       "abc123".to_string(),
            url:         "http://localhost:3000/register?token=abc123".to_string(),
            display_url: "http://localhost...token=abc123".to_string(),
        }
    }

    #[async_trait]
    impl Repository for MockRepo {
        async fn get_current(&self) -> Result<Option<Vo>, DomainError> {
            Ok(self.current.lock().unwrap().take().unwrap_or(None))
        }
        async fn issue(&self) -> Result<Vo, DomainError> {
            Ok(self.issue.lock().unwrap().take().unwrap_or_else(make_vo))
        }
        async fn find_by_token(&self, _: &str) -> Result<Option<Vo>, DomainError> {
            Ok(self.by_token.lock().unwrap().take().unwrap_or(None))
        }
    }

    fn make_uc(mock: Arc<MockRepo>) -> Interactor {
        Interactor::new(mock, Arc::new(MockAuthRepo))
    }

    #[tokio::test]
    async fn test_current_returns_vo() {
        let mock = Arc::new(MockRepo::new());
        *mock.current.lock().unwrap() = Some(Some(make_vo()));
        let uc = make_uc(mock);
        let vo = uc.current().await.unwrap();
        assert_eq!(vo.token, "abc123");
    }

    #[tokio::test]
    async fn test_current_returns_error_when_none() {
        let mock = Arc::new(MockRepo::new());
        *mock.current.lock().unwrap() = Some(None);
        let uc = make_uc(mock);
        assert!(uc.current().await.is_err());
    }

    #[tokio::test]
    async fn test_issue_returns_new_vo() {
        let mock = Arc::new(MockRepo::new());
        *mock.issue.lock().unwrap() = Some(make_vo());
        let uc = make_uc(mock);
        let vo = uc.issue().await.unwrap();
        assert!(!vo.token.is_empty());
        assert!(vo.url.contains(&vo.token));
    }

    #[tokio::test]
    async fn test_find_by_token_success() {
        let mock = Arc::new(MockRepo::new());
        *mock.by_token.lock().unwrap() = Some(Some(make_vo()));
        let uc = make_uc(mock);
        let vo = uc.find_by_token(FindByTokenDto { token: "abc123".to_string() }).await.unwrap();
        assert_eq!(vo.token, "abc123");
    }

    #[tokio::test]
    async fn test_find_by_token_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.by_token.lock().unwrap() = Some(None);
        let uc = make_uc(mock);
        let result = uc.find_by_token(FindByTokenDto { token: "invalid".to_string() }).await;
        assert!(result.is_err());
    }
}
