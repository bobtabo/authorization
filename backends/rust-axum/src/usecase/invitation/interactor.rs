//! 招待ユースケース Interactor モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

use std::sync::Arc;
use crate::domain::invitation::{value_objects::Vo, repository::Repository};
use super::dto::FindByTokenDto;

pub type UseCaseError = Box<dyn std::error::Error + Send + Sync>;

/// 招待のユースケース実装。
pub struct Interactor {
    repo: Arc<dyn Repository>,
}

impl Interactor {
    /// リポジトリを受け取りインタラクターを生成します。
    pub fn new(repo: Arc<dyn Repository>) -> Self {
        Self { repo }
    }

    /// 現在有効な招待トークンの VO を返します。存在しない場合はエラーを返します。
    pub async fn current(&self) -> Result<Vo, UseCaseError> {
        todo!()
    }

    /// 新しい招待トークンを発行して VO を返します。
    pub async fn issue(&self) -> Result<Vo, UseCaseError> {
        todo!()
    }

    /// トークン文字列で招待 VO を返します。無効な場合はエラーを返します。
    pub async fn find_by_token(&self, _dto: FindByTokenDto) -> Result<Vo, UseCaseError> {
        todo!()
    }
}
