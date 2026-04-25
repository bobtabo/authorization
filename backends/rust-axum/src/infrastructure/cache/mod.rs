//! Redis キャッシュインフラモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use crate::{
    config::Config,
    domain::gate::value_objects::{CacheRepository, DomainError},
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
}

#[async_trait]
impl CacheRepository for RedisGateRepository {
    /// キャッシュから JWT を取得します。キャッシュミス時は None を返します。
    async fn get_jwt(&self, _identifier: &str, _member_id: &str) -> Result<Option<String>, DomainError> {
        todo!()
    }
    /// JWT をキャッシュに保存します。
    async fn put_jwt(&self, _identifier: &str, _member_id: &str, _token: &str, _ttl: i64) -> Result<(), DomainError> {
        todo!()
    }
}
