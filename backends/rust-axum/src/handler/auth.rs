use axum::{extract::Path, http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn google_redirect() -> StatusCode {
    StatusCode::OK
}

pub async fn google_callback() -> StatusCode {
    StatusCode::OK
}

pub async fn get_my_profile() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn login() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn logout() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn invitation(Path(_token): Path<String>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}
