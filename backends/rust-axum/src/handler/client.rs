use axum::{extract::Path, http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn index() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!([])))
}

pub async fn store() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn show(Path(_id): Path<i64>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn update(Path(_id): Path<i64>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn destroy(Path(_id): Path<i64>) -> StatusCode {
    StatusCode::NO_CONTENT
}
