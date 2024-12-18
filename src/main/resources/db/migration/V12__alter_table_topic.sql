CREATE TABLE `topic`
(
    `topic_id` bigint NOT NULL AUTO_INCREMENT,
    `topic_name`      varchar(255) DEFAULT NULL,
    `subscribe`      bit(1),
    `user_id`        bigint       DEFAULT NULL,
    PRIMARY KEY (`topic_id`),
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);