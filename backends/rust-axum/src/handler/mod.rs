pub mod admin_invitation;
pub mod auth;
pub mod client;
pub mod gate;
pub mod notification;
pub mod staff;

use axum_extra::extract::CookieJar;

pub const TIME_FORMAT: &str = "%Y-%m-%d %H:%M";

pub fn staff_id_from_cookie(jar: &CookieJar) -> u32 {
    jar.get("staff_id")
        .and_then(|c| c.value().parse::<u32>().ok())
        .unwrap_or(0)
}
