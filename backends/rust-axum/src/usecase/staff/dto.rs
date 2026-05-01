//! スタッフユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// スタッフロール更新入力 DTO。
pub struct UpdateRoleDto {
    pub id:          u32,
    pub role:        i32,
    pub executor_id: u32,
}

/// スタッフ論理削除入力 DTO。
pub struct DestroyDto {
    pub id:          u32,
    pub executor_id: u32,
}
