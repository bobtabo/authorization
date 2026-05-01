//! Redis キャッシュインフラモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use redis::AsyncCommands;
use crate::{
    config::Config,
    domain::gate::value_objects::{CacheRepository, DomainError},
    domain::invitation::auth_repository::AuthRepository,
};
use redis::Client;

/// 設定から Redis クライアントを生成します。
pub fn new(cfg: &Config) -> redis::RedisResult<Client> {
    let url = if cfg.redis.password.is_empty() {
        format!("redis://{}/{}", cfg.redis.addr, cfg.redis.db)
    } else {
        format!("redis://:{}@{}/{}", cfg.redis.password, cfg.redis.addr, cfg.redis.db)
    };
    Client::open(url)
}

/// Redis を用いた Gate JWT キャッシュリポジトリ。
pub struct RedisGateRepository {
    client: Client,
    prefix: String,
}

impl RedisGateRepository {
    /// クライアントとキャッシュプレフィックスを受け取りリポジトリを生成します。
    pub fn new(client: Client, cfg: &Config) -> Self {
        Self {
            client,
            prefix: cfg.app.cache_prefix.clone(),
        }
    }

    fn cache_key(&self, identifier: &str, member_id: &str) -> String {
        format!("{}:gate.jwt:{}:{}", self.prefix, identifier, member_id)
    }
}

/// Redis を用いた招待認証キャッシュリポジトリ。
pub struct RedisInvitationAuthRepository {
    client: Client,
    prefix: String,
}

impl RedisInvitationAuthRepository {
    /// クライアントとキャッシュプレフィックスを受け取りリポジトリを生成します。
    pub fn new(client: Client, cfg: &Config) -> Self {
        Self {
            client,
            prefix: cfg.app.cache_prefix.clone(),
        }
    }

    fn cache_key(&self, token: &str) -> String {
        format!("{}:invitation_auth:invitation_auth:{}", self.prefix, token)
    }
}

#[async_trait]
impl AuthRepository for RedisInvitationAuthRepository {
    async fn store(&self, token: &str, ttl: u64) -> Result<(), DomainError> {
        let key = self.cache_key(token);
        let mut conn = self.client.get_multiplexed_async_connection().await?;
        let _: () = conn.set_ex(&key, token, ttl).await?;
        Ok(())
    }

    async fn find(&self, token: &str) -> Result<Option<String>, DomainError> {
        let key = self.cache_key(token);
        let mut conn = self.client.get_multiplexed_async_connection().await?;
        let val: Option<String> = conn.get(&key).await?;
        Ok(val)
    }

    async fn remove(&self, token: &str) -> Result<(), DomainError> {
        let key = self.cache_key(token);
        let mut conn = self.client.get_multiplexed_async_connection().await?;
        let _: () = conn.del(&key).await?;
        Ok(())
    }
}

#[async_trait]
impl CacheRepository for RedisGateRepository {
    async fn get_jwt(&self, identifier: &str, member_id: &str) -> Result<Option<String>, DomainError> {
        let key = self.cache_key(identifier, member_id);
        let mut conn = self.client.get_multiplexed_async_connection().await?;
        let val: Option<String> = conn.get(&key).await?;
        Ok(val)
    }

    async fn put_jwt(&self, identifier: &str, member_id: &str, token: &str, ttl: i64) -> Result<(), DomainError> {
        let key = self.cache_key(identifier, member_id);
        let mut conn = self.client.get_multiplexed_async_connection().await?;
        let _: () = conn.set_ex(&key, token, ttl as u64).await?;
        Ok(())
    }
}
