//! クライアントユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use chrono::{DateTime, Utc};
use crate::domain::client::{
    condition::Condition,
    entity::Client,
    enums::STATUS_INACTIVE,
    repository::Repository,
    value_objects::{DetailVo, ListItem, StoreResultVo},
};
use super::dto::{ListConditionDto, StoreDto, UpdateDto};

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// クライアントのユースケース実装。
pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    /// 検索条件に合致するクライアント一覧の VO を返します。
    pub async fn find_by_condition(&self, dto: ListConditionDto) -> Result<Vec<ListItem>, UseCaseError> {
        let cond = Condition {
            keyword:    dto.keyword,
            start_from: dto.start_from.and_then(|s| parse_datetime(&s)),
            start_to:   dto.start_to.and_then(|s| parse_datetime(&s)),
            statuses:   dto.statuses,
        };
        let clients = self.repo.find_by_condition(cond).await?;
        Ok(clients.into_iter().map(to_list_item).collect())
    }

    /// ID でクライアント詳細の VO を返します。存在しない場合はエラーを返します。
    pub async fn find_by_id(&self, id: u64) -> Result<DetailVo, UseCaseError> {
        let c = self.repo.find_by_id(id).await?
            .ok_or_else(|| simple_err("client_not_found"))?;
        Ok(to_detail_vo(c))
    }

    /// クライアントを新規登録し、登録結果の VO を返します。
    pub async fn store(&self, dto: StoreDto) -> Result<StoreResultVo, UseCaseError> {
        let (priv_pem, pub_pem, fingerprint) = generate_rsa_keys().await?;

        let mut token_bytes = [0u8; 32];
        rand::RngCore::fill_bytes(&mut rand::rngs::OsRng, &mut token_bytes);
        let access_token = hex::encode(token_bytes);

        let mut id_bytes = [0u8; 8];
        rand::RngCore::fill_bytes(&mut rand::rngs::OsRng, &mut id_bytes);
        let identifier = hex::encode(id_bytes);

        let now = chrono::Utc::now();
        let c = Client {
            id:           0,
            name:         dto.name,
            identifier,
            post_code:    dto.post_code,
            pref:         dto.pref,
            city:         dto.city,
            address:      dto.address,
            building:     dto.building,
            tel:          dto.tel,
            email:        dto.email,
            access_token,
            private_key:  priv_pem,
            public_key:   pub_pem,
            fingerprint,
            status:       STATUS_INACTIVE,
            start_at:     None,
            stop_at:      None,
            created_at:   now,
            created_by:   Some(dto.executor_id),
            updated_at:   now,
            updated_by:   Some(dto.executor_id),
            deleted_at:   None,
            deleted_by:   None,
            version:      0,
        };
        let saved = self.repo.save(c).await?;
        Ok(StoreResultVo {
            id:         saved.id,
            name:       saved.name,
            identifier: saved.identifier,
            email:      saved.email,
            token:      saved.access_token,
        })
    }

    /// クライアントを更新し、更新後の詳細 VO を返します。楽観排他エラー時は Err を返します。
    pub async fn update(&self, dto: UpdateDto) -> Result<DetailVo, UseCaseError> {
        let mut c = self.repo.find_by_id(dto.id).await?
            .ok_or_else(|| simple_err("client_not_found"))?;

        if c.version != dto.version {
            return Err("optimistic_lock_conflict".to_string().into());
        }

        if let Some(v) = dto.name        { c.name      = v; }
        if let Some(v) = dto.post_code   { c.post_code = v; }
        if let Some(v) = dto.pref        { c.pref      = v; }
        if let Some(v) = dto.city        { c.city      = v; }
        if let Some(v) = dto.address     { c.address   = v; }
        if let Some(v) = dto.building    { c.building  = v; }
        if let Some(v) = dto.tel         { c.tel       = v; }
        if let Some(v) = dto.email       { c.email     = v; }

        if let Some(status) = dto.status {
            let now = chrono::Utc::now();
            if status == 2 && c.start_at.is_none() {
                c.start_at = Some(now);
                c.stop_at  = None;
            }
            if status == 3 {
                c.stop_at = Some(now);
            }
            c.status = status;
        }

        let now = chrono::Utc::now();
        c.updated_at = now;
        c.updated_by = Some(dto.executor_id);

        let saved = self.repo.save(c).await?;
        Ok(to_detail_vo(saved))
    }

    /// クライアントを論理削除します。楽観排他エラー時は Err を返します。
    pub async fn destroy(&self, id: u64, executor_id: u32, version: i32) -> Result<(), UseCaseError> {
        let mut c = self.repo.find_by_id(id).await?
            .ok_or_else(|| simple_err("client_not_found"))?;

        if c.version != version {
            return Err("optimistic_lock_conflict".to_string().into());
        }

        let now = chrono::Utc::now();
        c.status     = 4;
        c.updated_at = now;
        c.updated_by = Some(executor_id);
        self.repo.save(c).await?;

        self.repo.soft_delete(id, executor_id).await?;
        Ok(())
    }

    /// Bearer トークンでクライアントを認証します。認証成功の場合 Some(Client) を返します。
    pub async fn find_by_access_token(&self, token: &str) -> Result<Option<Client>, UseCaseError> {
        Ok(self.repo.find_by_access_token(token).await?)
    }

    /// 識別子でクライアントを返します。存在しない場合は None を返します。
    pub async fn find_by_identifier(&self, identifier: &str) -> Result<Option<Client>, UseCaseError> {
        Ok(self.repo.find_by_identifier(identifier).await?)
    }
}

fn to_list_item(c: Client) -> ListItem {
    ListItem {
        id:         c.id,
        name:       c.name,
        status:     c.status,
        start_at:   c.start_at,
        stop_at:    c.stop_at,
        created_at: c.created_at,
        updated_at: c.updated_at,
    }
}

fn to_detail_vo(c: Client) -> DetailVo {
    DetailVo {
        id:         c.id,
        name:       c.name,
        identifier: c.identifier,
        post_code:  c.post_code,
        pref:       c.pref,
        city:       c.city,
        address:    c.address,
        building:   c.building,
        tel:        c.tel,
        email:      c.email,
        status:     c.status,
        start_at:   c.start_at,
        stop_at:    c.stop_at,
        created_at: c.created_at,
        updated_at: c.updated_at,
    }
}

fn parse_datetime(s: &str) -> Option<DateTime<Utc>> {
    use chrono::NaiveDateTime;
    NaiveDateTime::parse_from_str(s, "%Y-%m-%d %H:%M:%S").ok()
        .or_else(|| NaiveDateTime::parse_from_str(s, "%Y-%m-%d 00:00:00").ok())
        .map(|dt| DateTime::from_naive_utc_and_offset(dt, Utc))
}

fn simple_err(msg: &str) -> UseCaseError {
    msg.to_string().into()
}

async fn generate_rsa_keys() -> Result<(String, String, String), UseCaseError> {
    use rsa::{RsaPrivateKey, pkcs1::EncodeRsaPrivateKey, pkcs8::EncodePublicKey};
    use sha2::{Sha256, Digest};
    use base64::Engine;

    let (priv_pem, pub_pem, fingerprint) = tokio::task::spawn_blocking(|| {
        let mut rng = rand::rngs::OsRng;
        let priv_key = RsaPrivateKey::new(&mut rng, 4096)
            .map_err(|e| -> UseCaseError { e.to_string().into() })?;
        let pub_key = priv_key.to_public_key();

        let priv_pem = priv_key
            .to_pkcs1_pem(rsa::pkcs8::der::pem::LineEnding::LF)
            .map_err(|e| -> UseCaseError { e.to_string().into() })?
            .to_string();

        let pub_pem = pub_key
            .to_public_key_pem(rsa::pkcs8::der::pem::LineEnding::LF)
            .map_err(|e| -> UseCaseError { e.to_string().into() })?;

        let pub_der = pub_key
            .to_public_key_der()
            .map_err(|e| -> UseCaseError { e.to_string().into() })?;
        let hash = Sha256::digest(pub_der.as_bytes());
        let fingerprint = format!(
            "SHA256:{}",
            base64::engine::general_purpose::STANDARD_NO_PAD.encode(hash)
        );

        Ok::<_, UseCaseError>((priv_pem, pub_pem, fingerprint))
    })
    .await??;

    Ok((priv_pem, pub_pem, fingerprint))
}

#[cfg(test)]
mod tests {
    use super::*;
    use async_trait::async_trait;
    use std::sync::Mutex;
    use crate::domain::client::{
        condition::Condition,
        entity::Client,
        repository::{DomainError, Repository},
    };

    struct MockRepo {
        find_by_access_token: Mutex<Option<Option<Client>>>,
        find_by_identifier:   Mutex<Option<Option<Client>>>,
        find_by_id:           Mutex<Option<Option<Client>>>,
        find_by_condition:    Mutex<Option<Vec<Client>>>,
        save_result:          Mutex<Option<Client>>,
    }

    impl MockRepo {
        fn new() -> Self {
            Self {
                find_by_access_token: Mutex::new(None),
                find_by_identifier:   Mutex::new(None),
                find_by_id:           Mutex::new(None),
                find_by_condition:    Mutex::new(None),
                save_result:          Mutex::new(None),
            }
        }
    }

    fn make_client(id: u64) -> Client {
        let now = chrono::Utc::now();
        Client {
            id,
            name:         "Test Client".to_string(),
            identifier:   "abc123".to_string(),
            post_code:    "1234567".to_string(),
            pref:         "Tokyo".to_string(),
            city:         "Shibuya".to_string(),
            address:      "1-1-1".to_string(),
            building:     "".to_string(),
            tel:          "0312345678".to_string(),
            email:        "test@example.com".to_string(),
            access_token: "token123".to_string(),
            private_key:  "priv".to_string(),
            public_key:   "pub".to_string(),
            fingerprint:  "SHA256:abc".to_string(),
            status:       1,
            start_at:     None,
            stop_at:      None,
            created_at:   now,
            created_by:   None,
            updated_at:   now,
            updated_by:   None,
            deleted_at:   None,
            deleted_by:   None,
            version:      0,
        }
    }

    #[async_trait]
    impl Repository for MockRepo {
        async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<Client>, DomainError> {
            Ok(self.find_by_condition.lock().unwrap().take().unwrap_or_default())
        }
        async fn find_by_id(&self, _id: u64) -> Result<Option<Client>, DomainError> {
            Ok(self.find_by_id.lock().unwrap().take().unwrap_or(None))
        }
        async fn find_by_access_token(&self, _token: &str) -> Result<Option<Client>, DomainError> {
            Ok(self.find_by_access_token.lock().unwrap().take().unwrap_or(None))
        }
        async fn find_by_identifier(&self, _identifier: &str) -> Result<Option<Client>, DomainError> {
            Ok(self.find_by_identifier.lock().unwrap().take().unwrap_or(None))
        }
        async fn save(&self, _c: Client) -> Result<Client, DomainError> {
            Ok(self.save_result.lock().unwrap().take().unwrap_or_else(|| make_client(1)))
        }
        async fn soft_delete(&self, _id: u64, _deleted_by: u32) -> Result<(), DomainError> {
            Ok(())
        }
    }

    #[tokio::test]
    async fn test_find_by_access_token_returns_client() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_access_token.lock().unwrap() = Some(Some(make_client(1)));
        let uc = Interactor::new(mock);
        let result = uc.find_by_access_token("token123").await.unwrap();
        assert!(result.is_some());
        assert_eq!(result.unwrap().id, 1);
    }

    #[tokio::test]
    async fn test_find_by_access_token_returns_none_when_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_access_token.lock().unwrap() = Some(None);
        let uc = Interactor::new(mock);
        let result = uc.find_by_access_token("wrong_token").await.unwrap();
        assert!(result.is_none());
    }

    #[tokio::test]
    async fn test_find_by_id_returns_detail_vo() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(Some(make_client(42)));
        let uc = Interactor::new(mock);
        let result = uc.find_by_id(42).await.unwrap();
        assert_eq!(result.id, 42);
        assert_eq!(result.name, "Test Client");
    }

    #[tokio::test]
    async fn test_find_by_id_returns_error_when_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(None);
        let uc = Interactor::new(mock);
        let result = uc.find_by_id(99).await;
        assert!(result.is_err());
    }

    #[tokio::test]
    async fn test_find_by_condition_maps_to_list_items() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_condition.lock().unwrap() = Some(vec![make_client(1), make_client(2)]);
        let uc = Interactor::new(mock);
        let dto = ListConditionDto { keyword: None, start_from: None, start_to: None, statuses: vec![] };
        let result = uc.find_by_condition(dto).await.unwrap();
        assert_eq!(result.len(), 2);
        assert_eq!(result[0].id, 1);
        assert_eq!(result[1].id, 2);
    }

    #[tokio::test]
    async fn test_find_by_identifier_returns_client() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_identifier.lock().unwrap() = Some(Some(make_client(5)));
        let uc = Interactor::new(mock);
        let result = uc.find_by_identifier("abc123").await.unwrap();
        assert!(result.is_some());
    }

    #[tokio::test]
    async fn test_destroy_returns_error_when_client_not_found() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(None);
        let uc = Interactor::new(mock);
        let result = uc.destroy(99, 1, 0).await;
        assert!(result.is_err());
    }

    #[tokio::test]
    async fn test_destroy_returns_conflict_when_version_mismatch() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(Some(make_client(1)));
        let uc = Interactor::new(mock);
        // make_client sets version = 0, but we pass version = 99
        let result = uc.destroy(1, 1, 99).await;
        assert!(result.is_err());
        assert_eq!(result.err().unwrap().to_string(), "optimistic_lock_conflict");
    }

    #[tokio::test]
    async fn test_update_returns_conflict_when_version_mismatch() {
        let mock = Arc::new(MockRepo::new());
        *mock.find_by_id.lock().unwrap() = Some(Some(make_client(1)));
        let uc = Interactor::new(mock);
        let dto = UpdateDto {
            id: 1, name: None, post_code: None, pref: None, city: None,
            address: None, building: None, tel: None, email: None,
            status: None, executor_id: 1, version: 99,
        };
        let result = uc.update(dto).await;
        assert!(result.is_err());
        assert_eq!(result.err().unwrap().to_string(), "optimistic_lock_conflict");
    }
}
