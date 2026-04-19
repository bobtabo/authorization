use axum::{http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn index() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn issue() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}
