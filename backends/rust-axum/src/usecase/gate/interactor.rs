//! Gate ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::{
    config::Config,
    domain::{
        client::repository::Repository as ClientRepository,
        gate::value_objects::{CacheRepository, IssueVo, VerifyVo},
    },
};
use super::dto::{IssueDto, VerifyDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// Gate JWT 発行・検証のユースケース実装。
pub struct Interactor {
    client_repo: Arc<dyn ClientRepository>,
    cache:       Arc<dyn CacheRepository>,
    cfg:         Arc<Config>,
}

impl Interactor {
    /// リポジトリ・キャッシュ・設定を受け取りインタラクターを生成します。
    pub fn new(
        client_repo: Arc<dyn ClientRepository>,
        cache: Arc<dyn CacheRepository>,
        cfg: Arc<Config>,
    ) -> Self {
        Self { client_repo, cache, cfg }
    }

    /// アクセストークンを検証し JWT を発行して VO を返します。
    pub async fn issue_token(&self, dto: IssueDto) -> Result<IssueVo, UseCaseError> {
        let c = self.client_repo.find_by_access_token(&dto.access_token).await?
            .ok_or_else(|| -> UseCaseError { "client_not_found".to_string().into() })?;

        if let Ok(Some(cached)) = self.cache.get_jwt(&c.identifier, &dto.member_id).await {
            if !cached.is_empty() {
                return Ok(IssueVo { token: cached });
            }
        }

        let token = issue_jwt(
            &dto.member_id,
            &c.identifier,
            &c.private_key,
            &c.fingerprint,
            &self.cfg.jwt.issuer,
            self.cfg.jwt.ttl,
        )?;

        let _ = self.cache.put_jwt(&c.identifier, &dto.member_id, &token, self.cfg.jwt.cache_ttl).await;
        Ok(IssueVo { token })
    }

    /// JWT を検証してクレームを含む VO を返します。
    pub async fn verify(&self, dto: VerifyDto) -> Result<VerifyVo, UseCaseError> {
        let c = self.client_repo.find_by_identifier(&dto.identifier).await?
            .ok_or_else(|| -> UseCaseError { "client_not_found".to_string().into() })?;

        let claims = verify_jwt(&dto.identifier, &dto.token, &c.public_key, &self.cfg.jwt.issuer)?;
        Ok(VerifyVo { claims })
    }
}

fn issue_jwt(
    member_id: &str,
    identifier: &str,
    private_key_pem: &str,
    fingerprint: &str,
    issuer: &str,
    ttl: i64,
) -> Result<String, UseCaseError> {
    use jsonwebtoken::{encode, Algorithm, EncodingKey, Header};

    let now = chrono::Utc::now().timestamp();
    let claims = serde_json::json!({
        "iss": issuer,
        "sub": member_id,
        "aud": [identifier],
        "exp": now + ttl,
        "iat": now,
        "nbf": now,
        "jti": uuid::Uuid::new_v4().to_string(),
    });

    let mut header = Header::new(Algorithm::RS256);
    header.kid = Some(fingerprint.to_string());

    let encoding_key = EncodingKey::from_rsa_pem(private_key_pem.as_bytes())
        .map_err(|e| -> UseCaseError { e.to_string().into() })?;

    encode(&header, &claims, &encoding_key)
        .map_err(|e| -> UseCaseError { e.to_string().into() })
}

fn verify_jwt(
    identifier: &str,
    token_str: &str,
    public_key_pem: &str,
    issuer: &str,
) -> Result<serde_json::Value, UseCaseError> {
    use jsonwebtoken::{decode, Algorithm, DecodingKey, Validation};

    let mut validation = Validation::new(Algorithm::RS256);
    validation.set_audience(&[identifier]);
    validation.set_issuer(&[issuer]);

    let decoding_key = DecodingKey::from_rsa_pem(public_key_pem.as_bytes())
        .map_err(|e| -> UseCaseError { e.to_string().into() })?;

    let token_data = decode::<serde_json::Value>(token_str, &decoding_key, &validation)
        .map_err(|e| -> UseCaseError { e.to_string().into() })?;

    Ok(token_data.claims)
}

#[cfg(test)]
mod tests {
    use super::*;
    use async_trait::async_trait;
    use std::sync::Mutex;
    use crate::domain::{
        client::{
            condition::Condition,
            entity::Client,
            repository::{DomainError, Repository as ClientRepo},
        },
        gate::value_objects::{CacheRepository, DomainError as CacheDomainError},
    };
    use crate::config::{AppConfig, DbConfig, JwtConfig, MailConfig, OAuthConfig, RedisConfig};

    struct MockClientRepo {
        by_access_token:  Mutex<Option<Option<Client>>>,
        by_identifier:    Mutex<Option<Option<Client>>>,
    }

    impl MockClientRepo {
        fn new() -> Self {
            Self {
                by_access_token: Mutex::new(None),
                by_identifier:   Mutex::new(None),
            }
        }
    }

    struct MockCache {
        get_result: Mutex<Option<Option<String>>>,
    }

    impl MockCache {
        fn new() -> Self { Self { get_result: Mutex::new(None) } }
    }

    fn make_client() -> Client {
        let now = chrono::Utc::now();
        Client {
            id: 1, name: "C".to_string(), identifier: "id1".to_string(),
            post_code: "".to_string(), pref: "".to_string(), city: "".to_string(),
            address: "".to_string(), building: "".to_string(), tel: "".to_string(),
            email: "c@example.com".to_string(),
            access_token: "tok1".to_string(),
            private_key: "priv".to_string(), public_key: "pub".to_string(),
            fingerprint: "SHA256:abc".to_string(),
            status: 2, start_at: None, stop_at: None,
            created_at: now, created_by: None, updated_at: now, updated_by: None,
            deleted_at: None, deleted_by: None, version: 0,
        }
    }

    fn make_config() -> Arc<Config> {
        Arc::new(Config {
            app: AppConfig {
                env: "test".to_string(), port: "8080".to_string(),
                runtime: "rust".to_string(),
                frontend_url: "http://localhost:3000".to_string(),
                staff_cookie_lifetime: 60, notification_default_limit: 10,
                cache_prefix: "test".to_string(),
            },
            db: DbConfig { dsn: "".to_string() },
            redis: RedisConfig { addr: "localhost:6379".to_string(), password: "".to_string(), db: 0 },
            oauth: OAuthConfig {
                google_client_id: "".to_string(), google_client_secret: "".to_string(),
                google_redirect_url: "".to_string(),
                github_client_id: "".to_string(), github_client_secret: "".to_string(),
                github_redirect_url: "".to_string(),
            },
            jwt: JwtConfig { issuer: "authorization".to_string(), algorithm: "RS256".to_string(), ttl: 1800, cache_ttl: 1800 },
            mail: MailConfig {
                host: "localhost".to_string(), port: "1025".to_string(),
                username: "".to_string(), password: "".to_string(),
                from_address: "no-reply@example.com".to_string(),
                app_name: "Test".to_string(), app_env: "test".to_string(),
            },
        })
    }

    #[async_trait]
    impl ClientRepo for MockClientRepo {
        async fn find_by_condition(&self, _: Condition) -> Result<Vec<Client>, DomainError> { Ok(vec![]) }
        async fn find_by_id(&self, _: u64) -> Result<Option<Client>, DomainError> { Ok(None) }
        async fn find_by_access_token(&self, _: &str) -> Result<Option<Client>, DomainError> {
            Ok(self.by_access_token.lock().unwrap().take().unwrap_or(None))
        }
        async fn find_by_identifier(&self, _: &str) -> Result<Option<Client>, DomainError> {
            Ok(self.by_identifier.lock().unwrap().take().unwrap_or(None))
        }
        async fn save(&self, c: Client) -> Result<Client, DomainError> { Ok(c) }
        async fn soft_delete(&self, _: u64, _: u32) -> Result<(), DomainError> { Ok(()) }
    }

    #[async_trait]
    impl CacheRepository for MockCache {
        async fn get_jwt(&self, _: &str, _: &str) -> Result<Option<String>, CacheDomainError> {
            Ok(self.get_result.lock().unwrap().take().unwrap_or(None))
        }
        async fn put_jwt(&self, _: &str, _: &str, _: &str, _: i64) -> Result<(), CacheDomainError> {
            Ok(())
        }
    }

    #[tokio::test]
    async fn test_issue_token_returns_error_when_client_not_found() {
        let client_repo = Arc::new(MockClientRepo::new());
        *client_repo.by_access_token.lock().unwrap() = Some(None);
        let cache = Arc::new(MockCache::new());
        let uc = Interactor::new(client_repo, cache, make_config());
        let dto = IssueDto { access_token: "bad_token".to_string(), member_id: "m1".to_string() };
        assert!(uc.issue_token(dto).await.is_err());
    }

    #[tokio::test]
    async fn test_verify_returns_error_when_client_not_found() {
        let client_repo = Arc::new(MockClientRepo::new());
        *client_repo.by_identifier.lock().unwrap() = Some(None);
        let cache = Arc::new(MockCache::new());
        let uc = Interactor::new(client_repo, cache, make_config());
        let dto = VerifyDto { identifier: "no_such".to_string(), token: "bad.jwt.token".to_string() };
        assert!(uc.verify(dto).await.is_err());
    }

    #[tokio::test]
    async fn test_issue_token_returns_cached_token_when_cache_hit() {
        let client_repo = Arc::new(MockClientRepo::new());
        *client_repo.by_access_token.lock().unwrap() = Some(Some(make_client()));
        let cache = Arc::new(MockCache::new());
        *cache.get_result.lock().unwrap() = Some(Some("cached.jwt.token".to_string()));
        let uc = Interactor::new(client_repo, cache, make_config());
        let dto = IssueDto { access_token: "tok1".to_string(), member_id: "m1".to_string() };
        let result = uc.issue_token(dto).await.unwrap();
        assert_eq!(result.token, "cached.jwt.token");
    }
}
