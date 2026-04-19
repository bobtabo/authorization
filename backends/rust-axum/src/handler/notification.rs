use axum::{extract::Path, http::StatusCode, Json};
use serde_json::{json, Value};

pub async fn counts() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!({})))
}

pub async fn index() -> (StatusCode, Json<Value>) {
    (StatusCode::OK, Json(json!([])))
}

pub async fn read_all() -> StatusCode {
    StatusCode::NO_CONTENT
}

pub async fn read(Path(_id): Path<i64>) -> StatusCode {
    StatusCode::NO_CONTENT
}
