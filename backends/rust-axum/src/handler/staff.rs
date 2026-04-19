use axum::{extract::Path, http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn index() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!([])))
}

pub async fn update_role(Path(_id): Path<i64>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn restore(Path(_id): Path<i64>) -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn destroy(Path(_id): Path<i64>) -> StatusCode {
    StatusCode::NO_CONTENT
}
