//! クライアントユースケース DTO モジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

/// クライアント登録入力 DTO。
pub struct StoreDto {
    pub name:        String,
    pub post_code:   String,
    pub pref:        String,
    pub city:        String,
    pub address:     String,
    pub building:    String,
    pub tel:         String,
    pub email:       String,
    pub executor_id: u32,
}

/// クライアント更新入力 DTO。
pub struct UpdateDto {
    pub id:          u64,
    pub name:        Option<String>,
    pub post_code:   Option<String>,
    pub pref:        Option<String>,
    pub city:        Option<String>,
    pub address:     Option<String>,
    pub building:    Option<String>,
    pub tel:         Option<String>,
    pub email:       Option<String>,
    pub status:      Option<i32>,
    pub executor_id: u32,
    pub version:     i32,
}

/// クライアント一覧検索条件 DTO。
pub struct ListConditionDto {
    pub keyword:    Option<String>,
    pub start_from: Option<String>,
    pub start_to:   Option<String>,
    pub statuses:   Vec<i32>,
}

/// スマホ連携: 利用開始結果 VO。
pub struct StartResultVo {
    pub access_token: String,
}
