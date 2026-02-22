-- ─────────────────────────────────────────────────────────────
--  Chat Service – MySQL Schema
--  Run once before starting the application (ddl-auto: validate)
-- ─────────────────────────────────────────────────────────────

CREATE DATABASE IF NOT EXISTS chat_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE chat_db;

-- ── Chat Sessions ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_sessions (
    id            BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
     session_uuid  VARCHAR(36)         NOT NULL,
    user_id       VARCHAR(128)     NOT NULL,
    title         VARCHAR(255)     NOT NULL DEFAULT 'New Chat',
    is_favorite   TINYINT(1)       NOT NULL DEFAULT 0,
    is_deleted    TINYINT(1)       NOT NULL DEFAULT 0,
    message_count INT UNSIGNED     NOT NULL DEFAULT 0,
    created_at    DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_at    DATETIME(6)               DEFAULT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_session_uuid (session_uuid),
    INDEX idx_user_id           (user_id),
    INDEX idx_user_favorite     (user_id, is_favorite),
    INDEX idx_user_deleted      (user_id, is_deleted),
    INDEX idx_updated_at        (updated_at)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Chat Messages ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_messages (
    id           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    message_uuid VARCHAR(36)         NOT NULL,
    session_id   BIGINT UNSIGNED  NOT NULL,
    sender_type  ENUM('USER','ASSISTANT','SYSTEM') NOT NULL,
    sender_id    VARCHAR(128)              DEFAULT NULL,
    content      TEXT             NOT NULL,
    context_data JSON                      DEFAULT NULL,
    token_count  INT UNSIGNED              DEFAULT NULL,
    is_deleted   TINYINT(1)       NOT NULL DEFAULT 0,
    created_at   DATETIME(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_message_uuid (message_uuid),
    INDEX idx_session_id        (session_id),
    INDEX idx_session_created   (session_id, created_at),
    CONSTRAINT fk_message_session
    FOREIGN KEY (session_id) REFERENCES chat_sessions(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;