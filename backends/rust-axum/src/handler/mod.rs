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
use hmac::{Hmac, Mac};
use sha2::Sha256;
use std::time::{SystemTime, UNIX_EPOCH};

pub const TIME_FORMAT: &str = "%Y-%m-%d %H:%M";

/// staff_id クッキーの値を "{staffId}.{有効期限のUnix秒}.{HMAC-SHA256署名}" 形式で署名します。
/// secretを知らない第三者は staffId や有効期限を改ざんしても正しい署名を作成できないため、
/// クッキー値の改ざん（なりすまし）を防げます。有効期限も署名対象に含めることで、
/// Max-Age（クライアント側の自己申告に過ぎない）が切れた後の値を手動のCookieヘッダーで
/// 再送しても拒否できます。
pub fn sign_staff_id(staff_id: u32, secret: &str, lifetime_secs: i64) -> String {
    let expires_at = now_unix() + lifetime_secs;
    let payload = format!("{staff_id}.{expires_at}");
    format!("{payload}.{}", hmac_hex(&payload, secret))
}

/// 署名済み staff_id クッキーの値を検証し、staff_id を返します。
/// 署名が不正・形式不正・有効期限切れの場合は 0（未認証）を返します。
fn verify_staff_id(value: &str, secret: &str) -> u32 {
    let mut parts = value.splitn(3, '.');
    let (Some(id_part), Some(exp_part), Some(sig)) = (parts.next(), parts.next(), parts.next())
    else {
        return 0;
    };
    if id_part.is_empty() || exp_part.is_empty() || sig.is_empty() {
        return 0;
    }

    let payload = format!("{id_part}.{exp_part}");
    let Ok(mut mac) = Hmac::<Sha256>::new_from_slice(secret.as_bytes()) else {
        return 0;
    };
    mac.update(payload.as_bytes());
    let Ok(sig_bytes) = hex::decode(sig) else {
        return 0;
    };
    if mac.verify_slice(&sig_bytes).is_err() {
        return 0;
    }

    let (Ok(id), Ok(expires_at)) = (id_part.parse::<u32>(), exp_part.parse::<i64>()) else {
        return 0;
    };
    if now_unix() > expires_at {
        return 0;
    }

    id
}

fn hmac_hex(payload: &str, secret: &str) -> String {
    let mut mac =
        Hmac::<Sha256>::new_from_slice(secret.as_bytes()).expect("HMAC accepts keys of any size");
    mac.update(payload.as_bytes());
    hex::encode(mac.finalize().into_bytes())
}

fn now_unix() -> i64 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_secs() as i64)
        .unwrap_or(0)
}

/// Cookie から署名済みスタッフ ID を検証して取得します。未設定・署名不正・有効期限切れの場合は 0 を返します。
pub fn staff_id_from_cookie(jar: &CookieJar, secret: &str) -> u32 {
    jar.get("staff_id")
        .map(|c| verify_staff_id(c.value(), secret))
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
