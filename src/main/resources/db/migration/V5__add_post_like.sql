CREATE TABLE post_like
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    contest_post_id BIGINT NOT NULL,
    `created_at`    datetime(6) DEFAULT NULL,
    `updated_at`    datetime(6) DEFAULT NULL,
    UNIQUE (user_id, contest_post_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (contest_post_id) REFERENCES contest_post (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE contest_post DROP COLUMN like_count;