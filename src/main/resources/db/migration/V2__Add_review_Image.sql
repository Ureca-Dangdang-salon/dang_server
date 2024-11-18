CREATE TABLE review_image (
                              `id`                 bigint NOT NULL AUTO_INCREMENT,
                              `image_key`          varchar(255) DEFAULT NULL,
                              `review_id`          bigint       DEFAULT NULL,
                              `created_at`         datetime(6) DEFAULT NULL,
                              `updated_at`         datetime(6) DEFAULT NULL,
                              PRIMARY KEY (`id`),
                              FOREIGN KEY (`review_id`) REFERENCES review (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;