-- jOOQ コード生成専用の DDL（DDLDatabase）。
-- 実スキーマは backends/php-laravel/database/migrations が正本であり、
-- このファイルはそれをミラーしたものです。マイグレーションを変更した場合は
-- 必ずこのファイルも合わせて更新してください。

CREATE TABLE clients (
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255)    NOT NULL,
    identifier   VARCHAR(255)    NOT NULL,
    post_code    VARCHAR(8)      NOT NULL,
    pref         VARCHAR(50)     NOT NULL,
    city         VARCHAR(100)    NOT NULL,
    address      VARCHAR(255)    NOT NULL,
    building     VARCHAR(255)    NULL,
    tel          VARCHAR(255)    NOT NULL,
    email        VARCHAR(255)    NOT NULL,
    access_token VARCHAR(512)    NOT NULL,
    private_key  TEXT            NOT NULL,
    public_key   TEXT            NOT NULL,
    fingerprint  VARCHAR(255)    NOT NULL,
    status       INT UNSIGNED    NOT NULL,
    start_at     TIMESTAMP       NULL,
    stop_at      TIMESTAMP       NULL,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   INT UNSIGNED    NOT NULL,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   INT UNSIGNED    NOT NULL,
    deleted_at   TIMESTAMP       NULL,
    deleted_by   INT UNSIGNED    NULL,
    version      INT UNSIGNED    NOT NULL DEFAULT 1
);

CREATE TABLE staffs (
    id            INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    provider      INT          NOT NULL,
    provider_id   VARCHAR(255) NOT NULL,
    avatar        TEXT         NULL,
    role          INT UNSIGNED NOT NULL,
    last_login_at TIMESTAMP    NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    INT UNSIGNED NOT NULL,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    INT UNSIGNED NOT NULL,
    deleted_at    TIMESTAMP    NULL,
    deleted_by    INT UNSIGNED NULL,
    version       INT UNSIGNED NOT NULL DEFAULT 1,
    CONSTRAINT staffs_email_unique UNIQUE (email)
);

CREATE TABLE invitations (
    id         INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token      VARCHAR(255) NOT NULL,
    role       INT UNSIGNED NOT NULL DEFAULT 2,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT UNSIGNED NOT NULL,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INT UNSIGNED NOT NULL,
    deleted_at TIMESTAMP    NULL,
    deleted_by INT UNSIGNED NULL,
    version    INT UNSIGNED NOT NULL DEFAULT 1,
    CONSTRAINT invitations_token_unique UNIQUE (token)
);

CREATE TABLE notifications (
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    staff_id     INT UNSIGNED    NOT NULL,
    message_type INT UNSIGNED    NOT NULL,
    title        VARCHAR(255)    NOT NULL,
    message      VARCHAR(512)    NOT NULL,
    url          VARCHAR(255)    NULL,
    `read`       TINYINT UNSIGNED NOT NULL DEFAULT 0,
    created_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   INT UNSIGNED    NOT NULL,
    updated_at   TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   INT UNSIGNED    NOT NULL,
    deleted_at   TIMESTAMP       NULL,
    deleted_by   INT UNSIGNED    NULL,
    version      INT UNSIGNED    NOT NULL DEFAULT 1,
    CONSTRAINT notifications_staff_id_foreign FOREIGN KEY (staff_id) REFERENCES staffs (id)
);

CREATE TABLE jwt_histories (
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    client_id  BIGINT NOT NULL,
    member_id  VARCHAR(255)    NOT NULL,
    issue_at   TIMESTAMP       NOT NULL,
    jwt        TEXT            NOT NULL,
    created_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by INT UNSIGNED    NOT NULL,
    updated_at TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by INT UNSIGNED    NOT NULL,
    deleted_at TIMESTAMP       NULL,
    deleted_by INT UNSIGNED    NULL,
    version    INT UNSIGNED    NOT NULL DEFAULT 1,
    CONSTRAINT jwt_histories_client_id_foreign FOREIGN KEY (client_id) REFERENCES clients (id)
);
