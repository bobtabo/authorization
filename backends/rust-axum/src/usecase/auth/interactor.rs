//! 認証ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::staff::{repository::Repository, value_objects::Vo};
use super::dto::LoginDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 認証のユースケース実装。
pub struct Interactor {
    staff_repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(staff_repo: Arc<dyn Repository>) -> Self {
        Self { staff_repo }
    }

    /// スタッフ ID でログイン中スタッフの VO を返します。
    pub async fn find_user(&self, _id: u32) -> Result<Vo, UseCaseError> {
        todo!()
    }

    /// OAuth 情報でスタッフを upsert してログインし、VO を返します。
    pub async fn login(&self, _dto: LoginDto) -> Result<Vo, UseCaseError> {
        todo!()
    }
}
