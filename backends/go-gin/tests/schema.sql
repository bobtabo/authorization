-- Goテスト用テーブル定義（PHP Migrationと同等）
-- TestMain の起動時に DROP→CREATE でクリーンな状態を保証する

SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `invitations`;
DROP TABLE IF EXISTS `clients`;
DROP TABLE IF EXISTS `staffs`;

SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE `staffs` (
    `id`            INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(100)    NOT NULL,
    `email`         VARCHAR(255)    NOT NULL,
    `provider`      INT             NOT NULL,
    `provider_id`   VARCHAR(255)    NOT NULL,
    `avatar`        TEXT,
    `role`          INT UNSIGNED    NOT NULL DEFAULT 2,
    `last_login_at` TIMESTAMP       NULL     DEFAULT NULL,
    `created_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by`    INT UNSIGNED,
    `updated_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`    INT UNSIGNED,
    `deleted_at`    TIMESTAMP       NULL,
    `deleted_by`    INT UNSIGNED    NULL,
    `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `staffs_email_unique` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `clients` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`          VARCHAR(255)    NOT NULL,
    `identifier`    VARCHAR(255)    NOT NULL,
    `post_code`     VARCHAR(8),
    `pref`          VARCHAR(50),
    `city`          VARCHAR(100),
    `address`       VARCHAR(255),
    `building`      VARCHAR(255),
    `tel`           VARCHAR(255),
    `email`         VARCHAR(255),
    `access_token`  VARCHAR(512),
    `private_key`   TEXT,
    `public_key`    TEXT,
    `fingerprint`   VARCHAR(255),
    `status`        INT UNSIGNED    NOT NULL DEFAULT 1,
    `start_at`      TIMESTAMP       NULL,
    `stop_at`       TIMESTAMP       NULL,
    `created_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by`    INT UNSIGNED,
    `updated_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`    INT UNSIGNED,
    `deleted_at`    TIMESTAMP       NULL,
    `deleted_by`    INT UNSIGNED    NULL,
    `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_clients_identifier` (`identifier`),
    UNIQUE KEY `idx_clients_access_token` (`access_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `invitations` (
    `id`            INT UNSIGNED    NOT NULL AUTO_INCREMENT,
    `token`         VARCHAR(255)    NOT NULL,
    `created_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by`    INT UNSIGNED,
    `updated_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`    INT UNSIGNED,
    `deleted_at`    TIMESTAMP       NULL,
    `deleted_by`    INT UNSIGNED    NULL,
    `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `invitations_token_unique` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `notifications` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `staff_id`      INT UNSIGNED    NOT NULL,
    `message_type`  INT UNSIGNED    NOT NULL DEFAULT 1,
    `title`         VARCHAR(255)    NOT NULL,
    `message`       VARCHAR(512)    NOT NULL DEFAULT '',
    `url`           VARCHAR(255)    NULL,
    `read`          TINYINT(1)      NOT NULL DEFAULT 0,
    `created_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by`    INT UNSIGNED,
    `updated_at`    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `updated_by`    INT UNSIGNED,
    `deleted_at`    TIMESTAMP       NULL,
    `deleted_by`    INT UNSIGNED    NULL,
    `version`       INT UNSIGNED    NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
