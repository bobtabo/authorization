//! ハンドラーモジュール。
//!
//! # Author
//! Satoshi Nagashiba <satoshi.nagashiba@gmail.com>

pub mod admin_invitation;
pub mod auth;
pub mod client;
pub mod gate;
pub mod mobile_client;
pub mod notification;
pub mod staff;

use axum_extra::extract::CookieJar;

pub const TIME_FORMAT: &str = "%Y-%m-%d %H:%M";

/// Cookie からスタッフ ID を取得します。未設定または不正値の場合は 0 を返します。
pub fn staff_id_from_cookie(jar: &CookieJar) -> u32 {
    jar.get("staff_id")
        .and_then(|c| c.value().parse::<u32>().ok())
        .unwrap_or(0)
}

/// クエリ文字列から整数の配列パラメータを取得します。
///
/// ブラケット付き（`key[]=1&key[]=2`）・ブラケット無しの繰り返しキー
/// （`key=1&key=2`、OpenAPI 仕様の `style: form, explode: true`）・
/// カンマ区切り（`key=1,2`）のいずれの形式にも対応します。axum の `Query`
/// 抽出（serde_urlencoded）は繰り返しキーを配列として扱えないため、
/// 生のクエリ文字列を自前で解析します。
pub fn int_array_query(raw_query: Option<&str>, key: &str) -> Vec<i32> {
    let Some(query) = raw_query else {
        return Vec::new();
    };
    let bracket_key = format!("{key}[]");
    query
        .split('&')
        .filter_map(|pair| pair.split_once('=').or(Some((pair, ""))))
        .filter(|(raw_key, _)| {
            let decoded = percent_decode(raw_key);
            decoded == key || decoded == bracket_key
        })
        .flat_map(|(_, raw_value)| {
            percent_decode(raw_value)
                .split(',')
                .filter_map(|v| v.trim().parse::<i32>().ok())
                .collect::<Vec<_>>()
        })
        .collect()
}

/// パーセントエンコーディングと `+` を含むクエリ文字列の断片をデコードします。
fn percent_decode(value: &str) -> String {
    let bytes = value.replace('+', " ").into_bytes();
    let mut decoded = Vec::with_capacity(bytes.len());
    let mut index = 0;
    while index < bytes.len() {
        if bytes[index] == b'%' && index + 2 < bytes.len() {
            if let Ok(byte) = u8::from_str_radix(
                &format!("{}{}", bytes[index + 1] as char, bytes[index + 2] as char),
                16,
            ) {
                decoded.push(byte);
                index += 3;
                continue;
            }
        }
        decoded.push(bytes[index]);
        index += 1;
    }
    String::from_utf8_lossy(&decoded).into_owned()
}
