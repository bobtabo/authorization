use axum::{extract::Path, http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn issue() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn verify(Path(_identifier): Path<String>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}
