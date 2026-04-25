//! スタッフリポジトリ SQLx 実装モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use async_trait::async_trait;
use sqlx::MySqlPool;
use crate::domain::staff::{
    condition::Condition,
    entity::Staff,
    repository::{DomainError, Repository},
};

/// SQLx を用いたスタッフリポジトリ実装。
pub struct SqlxStaffRepository {
    pool: MySqlPool,
}

impl SqlxStaffRepository {
    /// プールを受け取りリポジトリを生成します。
    pub fn new(pool: MySqlPool) -> Self {
        Self { pool }
    }
}

#[async_trait]
impl Repository for SqlxStaffRepository {
    async fn find_by_condition(&self, _cond: Condition) -> Result<Vec<Staff>, DomainError> {
        todo!()
    }
    async fn find_by_id(&self, _id: u32) -> Result<Option<Staff>, DomainError> {
        todo!()
    }
    async fn find_by_provider(&self, _provider: i32, _provider_id: &str) -> Result<Option<Staff>, DomainError> {
        todo!()
    }
    async fn find_all_active(&self) -> Result<Vec<Staff>, DomainError> {
        todo!()
    }
    async fn save(&self, _s: Staff) -> Result<Staff, DomainError> {
        todo!()
    }
    async fn update_role(&self, _id: u32, _role: i32, _updated_by: u32) -> Result<bool, DomainError> {
        todo!()
    }
    async fn soft_delete(&self, _id: u32, _deleted_by: u32) -> Result<bool, DomainError> {
        todo!()
    }
    async fn restore(&self, _id: u32) -> Result<bool, DomainError> {
        todo!()
    }
}
