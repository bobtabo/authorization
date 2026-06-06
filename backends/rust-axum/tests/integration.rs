mod common;

use axum::http::{Request, StatusCode, header};
use http_body_util::BodyExt;
use tower::ServiceExt;

// ── Auth ──────────────────────────────────────────────────────────────────────

#[tokio::test]
async fn get_auth_me_returns_profile_when_authenticated() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;

    let req = Request::builder()
        .uri("/api/auth/me")
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let body = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert_eq!(json["staff_id"], staff_id);
}

#[tokio::test]
async fn get_auth_me_returns_401_when_unauthenticated() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;

    let req = Request::builder()
        .uri("/api/auth/me")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::UNAUTHORIZED);
}

// ── Clients ───────────────────────────────────────────────────────────────────

#[tokio::test]
async fn get_clients_returns_list() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    common::create_client(&pool).await;
    common::create_client(&pool).await;

    let req = Request::builder()
        .uri("/api/clients")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let body = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&body).unwrap();
    assert_eq!(json["data"].as_array().unwrap().len(), 2);
    assert!(json["pager"].is_object());
}

#[tokio::test]
async fn post_clients_store_creates_client() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;

    let body = serde_json::json!({
        "name": "新規クライアント",
        "post_code": "100-0001",
        "pref": "東京都",
        "city": "千代田区",
        "address": "千代田1-1",
        "building": "",
        "tel": "0312345678",
        "email": "new-client@example.com"
    });

    let req = Request::builder()
        .method("POST")
        .uri("/api/clients/store")
        .header(header::CONTENT_TYPE, "application/json")
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::from(body.to_string()))
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::CREATED);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert!(json["id"].as_u64().unwrap() > 0);
}

#[tokio::test]
async fn get_clients_show_returns_client() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let client = common::create_client(&pool).await;

    let req = Request::builder()
        .uri(format!("/api/clients/{}", client.id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["id"], client.id);
}

#[tokio::test]
async fn delete_clients_destroy_soft_deletes_client() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;
    let client   = common::create_client(&pool).await;

    let req = Request::builder()
        .method("DELETE")
        .uri(format!("/api/clients/{}/delete", client.id))
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .header(header::CONTENT_TYPE, "application/json")
        .body(axum::body::Body::from(r#"{"version":1}"#))
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);
}

// ── Staffs ────────────────────────────────────────────────────────────────────

#[tokio::test]
async fn get_staffs_returns_list() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    common::create_staff(&pool).await;
    common::create_staff(&pool).await;

    let req = Request::builder()
        .uri("/api/staffs")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["data"].as_array().unwrap().len(), 2);
    assert!(json["pager"].is_object());
}

#[tokio::test]
async fn patch_staffs_restore_restores_deleted_staff() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;

    let now = chrono::Local::now().naive_local();
    sqlx::query("UPDATE staffs SET deleted_at = ? WHERE id = ?")
        .bind(now)
        .bind(staff_id)
        .execute(&pool)
        .await
        .unwrap();

    let req = Request::builder()
        .method("PATCH")
        .uri(format!("/api/staffs/{}/restore", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["id"], staff_id);
}

#[tokio::test]
async fn delete_staffs_destroy_soft_deletes_staff() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let executor_id = common::create_staff(&pool).await;
    let target_id   = common::create_staff(&pool).await;

    let req = Request::builder()
        .method("DELETE")
        .uri(format!("/api/staffs/{}/delete", target_id))
        .header(header::COOKIE, format!("staff_id={}", executor_id))
        .header(header::CONTENT_TYPE, "application/json")
        .body(axum::body::Body::from(r#"{"version":1}"#))
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);
}

// ── Admin Invitation ──────────────────────────────────────────────────────────

#[tokio::test]
async fn get_admin_invitation_returns_existing_invitation() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let token = common::create_invitation(&pool, 2).await;

    let req = Request::builder()
        .uri("/api/admin/invitation?role=2")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["found"], true);
    assert_eq!(json["token"], token);
}

#[tokio::test]
async fn get_admin_invitation_returns_existing_admin_invitation() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let token = common::create_invitation(&pool, 1).await;

    let req = Request::builder()
        .uri("/api/admin/invitation?role=1")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["found"], true);
    assert_eq!(json["token"], token);
}

#[tokio::test]
async fn get_admin_invitation_returns_400_for_invalid_role() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;

    let req = Request::builder()
        .uri("/api/admin/invitation?role=3")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::BAD_REQUEST);
}

#[tokio::test]
async fn get_admin_invitation_issue_requires_auth() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;

    let req = Request::builder()
        .uri("/api/admin/invitation/issue?role=2")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::UNAUTHORIZED);
}

#[tokio::test]
async fn get_admin_invitation_issue_creates_new_invitation() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;

    let req = Request::builder()
        .uri("/api/admin/invitation/issue?role=2")
        .header(axum::http::header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["found"], true);
    assert!(json["token"].as_str().is_some());
}

// ── Gate ──────────────────────────────────────────────────────────────────────

#[tokio::test]
async fn get_gate_issue_returns_jwt_token() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let client = common::create_client(&pool).await;

    let req = Request::builder()
        .uri("/api/gate/issue?member=user-001")
        .header(header::AUTHORIZATION, format!("Bearer {}", client.access_token))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert!(json["token"].as_str().is_some());
}

#[tokio::test]
async fn get_gate_verify_returns_claims() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let client = common::create_client(&pool).await;

    // issue
    let issue_req = Request::builder()
        .uri("/api/gate/issue?member=user-001")
        .header(header::AUTHORIZATION, format!("Bearer {}", client.access_token))
        .body(axum::body::Body::empty())
        .unwrap();
    let issue_res = app.clone().oneshot(issue_req).await.unwrap();
    let issue_bytes = issue_res.into_body().collect().await.unwrap().to_bytes();
    let issue_json: serde_json::Value = serde_json::from_slice(&issue_bytes).unwrap();
    let token = issue_json["token"].as_str().unwrap().to_string();

    // verify
    let req = Request::builder()
        .uri(format!("/api/gate/client/{}/verify?token={}", client.identifier, token))
        .body(axum::body::Body::empty())
        .unwrap();
    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["sub"], "user-001");
}

// ── Notifications ─────────────────────────────────────────────────────────────

#[tokio::test]
async fn get_notifications_counts_returns_unread_and_total() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;
    common::create_notification(&pool, staff_id, "通知1", false).await;
    common::create_notification(&pool, staff_id, "通知2", true).await;

    let req = Request::builder()
        .uri("/api/notifications/counts")
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["total"], 2);
    assert_eq!(json["unread"], 1);
}

#[tokio::test]
async fn get_notifications_counts_returns_401_when_unauthenticated() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;

    let req = Request::builder()
        .uri("/api/notifications/counts")
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::UNAUTHORIZED);
}

#[tokio::test]
async fn get_notifications_returns_list() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;
    common::create_notification(&pool, staff_id, "通知1", false).await;
    common::create_notification(&pool, staff_id, "通知2", false).await;

    let req = Request::builder()
        .uri("/api/notifications")
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["items"].as_array().unwrap().len(), 2);
}

#[tokio::test]
async fn patch_notifications_bulk_marks_all_read() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;
    common::create_notification(&pool, staff_id, "通知1", false).await;
    common::create_notification(&pool, staff_id, "通知2", false).await;

    let req = Request::builder()
        .method("PATCH")
        .uri("/api/notifications")
        .header(header::COOKIE, format!("staff_id={}", staff_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["updated"], 2);
}

#[tokio::test]
async fn patch_notifications_id_marks_single_notification_as_read() {
    let (app, pool) = common::build_test_app().await;
    common::truncate_tables(&pool).await;
    let staff_id = common::create_staff(&pool).await;
    let notif_id = common::create_notification(&pool, staff_id, "通知1", false).await;

    let req = Request::builder()
        .method("PATCH")
        .uri(format!("/api/notifications/{}", notif_id))
        .body(axum::body::Body::empty())
        .unwrap();

    let res = app.oneshot(req).await.unwrap();
    assert_eq!(res.status(), StatusCode::OK);

    let bytes = res.into_body().collect().await.unwrap().to_bytes();
    let json: serde_json::Value = serde_json::from_slice(&bytes).unwrap();
    assert_eq!(json["id"], notif_id);
}
