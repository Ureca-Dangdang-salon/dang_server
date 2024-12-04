DROP TABLE notification;

CREATE TABLE `fcm_token`
(
    `fcm_token_id` bigint NOT NULL AUTO_INCREMENT,
    `fcm_token`      varchar(255) DEFAULT NULL,
    `last_user_at`   datetime(6) DEFAULT NULL,
    `user_id`        bigint       DEFAULT NULL,
    PRIMARY KEY (`fcm_token_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

ALTER TABLE users
    ADD COLUMN `notification_enabled` bit(1);
