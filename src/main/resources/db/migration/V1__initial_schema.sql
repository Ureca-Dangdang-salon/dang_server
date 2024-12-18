CREATE TABLE `badge`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `image_key`  varchar(255) DEFAULT NULL,
    `name`       varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `city`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `name`       varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feature`
(
    `feature_id`  bigint NOT NULL AUTO_INCREMENT,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `is_custom`   bit(1)       DEFAULT NULL,
    PRIMARY KEY (`feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `service`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    `is_custom`   bit(1)       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `district`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `name`       varchar(255) DEFAULT NULL,
    `city_id`    bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKsgx09prp6sk2f0we38bf2dtal` (`city_id`),
    CONSTRAINT `FKsgx09prp6sk2f0we38bf2dtal` FOREIGN KEY (`city_id`) REFERENCES `city` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    `email`       varchar(255) DEFAULT NULL,
    `image_key`   varchar(255) DEFAULT NULL,
    `name`        varchar(255) DEFAULT NULL,
    `username`    varchar(255) DEFAULT NULL,
    `role`        enum('ROLE_ADMIN','ROLE_SALON','ROLE_USER','ROLE_PENDING') DEFAULT NULL,
    `district_id` bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FKq6a9571l40g6c02up8o4ky79b` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dog_profile`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `month`      int          DEFAULT NULL,
    `year`       int          DEFAULT NULL,
    `gender`     enum('FEMALE','MALE') DEFAULT NULL,
    `image_key`  varchar(255) DEFAULT NULL,
    `name`       varchar(255) DEFAULT NULL,
    `neutering`  enum('N','Y') DEFAULT NULL,
    `species`    varchar(255) DEFAULT NULL,
    `weight`     int    NOT NULL,
    `user_id`    bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY          `FK8dv8uxmiead9ibrmomyq9ntp7` (`user_id`),
    CONSTRAINT `FK8dv8uxmiead9ibrmomyq9ntp7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `district_service`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    `district_id` bigint DEFAULT NULL,
    `profile_id`  bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY           `FKgy6sxmyws2iek0q68ydyueajj` (`district_id`),
    KEY           `FKnyllignrxg8cu6582avprepnt` (`profile_id`),
    CONSTRAINT `FKgy6sxmyws2iek0q68ydyueajj` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`),
    CONSTRAINT `FKnyllignrxg8cu6582avprepnt` FOREIGN KEY (`profile_id`) REFERENCES `dog_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `groomer_profile`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `contact_hours`   varchar(255) DEFAULT NULL,
    `address`         varchar(255) DEFAULT NULL,
    `business_number` varchar(255) DEFAULT NULL,
    `certification`   varchar(255) DEFAULT NULL,
    `experience`      varchar(255) DEFAULT NULL,
    `description`     text,
    `faq`             text,
    `start_chat`      varchar(255) DEFAULT NULL,
    `image_key`       varchar(255) DEFAULT NULL,
    `name`            varchar(255) DEFAULT NULL,
    `phone`           varchar(255) DEFAULT NULL,
    `service_type`    enum('ANY','SHOP','VISIT') DEFAULT NULL,
    `user_id`         bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKm43y06xr4jr935g6futso0tmx` (`user_id`),
    CONSTRAINT `FKgc57p2busxulf0egn8knl2h1s` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `groomer_profile_service`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `profile_id` bigint DEFAULT NULL,
    `service_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKrn9rbmjhpbj38fpfqash9o03o` (`profile_id`),
    KEY          `FKfyec4ebx922fvoovksrw40y73` (`service_id`),
    CONSTRAINT `FKfyec4ebx922fvoovksrw40y73` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
    CONSTRAINT `FKrn9rbmjhpbj38fpfqash9o03o` FOREIGN KEY (`profile_id`) REFERENCES `groomer_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `groomer_profile_pictures`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `image_key`  varchar(255) DEFAULT NULL,
    `profile_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY          `FK99v3xt7gtok7wq1f75six5x07` (`profile_id`),
    CONSTRAINT `FK99v3xt7gtok7wq1f75six5x07` FOREIGN KEY (`profile_id`) REFERENCES `groomer_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `groomer_badge`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `badge_id`   bigint DEFAULT NULL,
    `profile_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKqudot3gj25rp1j4jklaek4ls3` (`badge_id`),
    KEY          `FKhiijgrbrxnsvo4pbgqb72r97r` (`profile_id`),
    CONSTRAINT `FKhiijgrbrxnsvo4pbgqb72r97r` FOREIGN KEY (`profile_id`) REFERENCES `groomer_profile` (`id`),
    CONSTRAINT `FKqudot3gj25rp1j4jklaek4ls3` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contest_post`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `created_at`         datetime(6) DEFAULT NULL,
    `updated_at`         datetime(6) DEFAULT NULL,
    `description`        varchar(255) DEFAULT NULL,
    `dog_name`           varchar(255) DEFAULT NULL,
    `image_key`          varchar(255) DEFAULT NULL,
    `like_count`         int          DEFAULT '0',
    `contest_id`         bigint       DEFAULT NULL,
    `groomer_profile_id` bigint NOT NULL,
    `user_id`            bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FKp0ahhbideeq4wl0j71k0w9qey` (`groomer_profile_id`),
    KEY                  `FKsruxnbgdbee6l78bxnxejmtdx` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `contest`
(
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `created_at`     datetime(6) DEFAULT NULL,
    `updated_at`     datetime(6) DEFAULT NULL,
    `description`    varchar(255) DEFAULT NULL,
    `end_at`         datetime(6) DEFAULT NULL,
    `started_at`     datetime(6) DEFAULT NULL,
    `title`          varchar(255) DEFAULT NULL,
    `winner_post_id` bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKkkyqo0k63mv2cf4nue8keqbbw` (`winner_post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `contest`
    ADD CONSTRAINT `FKpep7f3ti8l80x8uq6k1c85sfl`
        FOREIGN KEY (`winner_post_id`) REFERENCES `contest_post` (`id`);

ALTER TABLE `contest_post`
    ADD CONSTRAINT `FKgs14lxptpb7hbcrwabhug9jk6`
        FOREIGN KEY (`contest_id`) REFERENCES `contest` (`id`);

CREATE TABLE `estimate_request`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `created_at`          datetime(6) DEFAULT NULL,
    `updated_at`          datetime(6) DEFAULT NULL,
    `aggression`          bit(1) NOT NULL,
    `current_photo_key`   varchar(255) DEFAULT NULL,
    `health_issue`        bit(1)       DEFAULT NULL,
    `request_date`        datetime(6) DEFAULT NULL,
    `request_status`      enum('CANCEL','COMPLETED','PAID','PENDING','REFUND') DEFAULT NULL,
    `service_type`        enum('ANY','SHOP','VISIT') DEFAULT NULL,
    `style_ref_photo_key` varchar(255) DEFAULT NULL,
    `district_id`         bigint       DEFAULT NULL,
    `user_id`             bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                   `FKn4wlayyiyu8hrwscqtls19x0e` (`district_id`),
    KEY                   `FKjmveywmjo8qkp6q0af9uvp57v` (`user_id`),
    CONSTRAINT `FKjmveywmjo8qkp6q0af9uvp57v` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FKn4wlayyiyu8hrwscqtls19x0e` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `estimate`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `created_at`          datetime(6) DEFAULT NULL,
    `updated_at`          datetime(6) DEFAULT NULL,
    `aggression_charge`   int    NOT NULL,
    `description`         text,
    `health_issue_charge` int    NOT NULL,
    `image_key`           varchar(255) DEFAULT NULL,
    `status`              enum('ACCEPTED','REJECTED','SEND', 'PAID', 'REFUND') DEFAULT NULL,
    `request_id`          bigint       DEFAULT NULL,
    `groomer_profile_id`  bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                   `FKr2cdpc6qr1bqa8km6na79v9qs` (`request_id`),
    KEY                   `FKcyqcgry2sj96ww6fb5y3531jf` (`groomer_profile_id`),
    CONSTRAINT `FKcyqcgry2sj96ww6fb5y3531jf` FOREIGN KEY (`groomer_profile_id`) REFERENCES `groomer_profile` (`id`),
    CONSTRAINT `FKr2cdpc6qr1bqa8km6na79v9qs` FOREIGN KEY (`request_id`) REFERENCES `estimate_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_room`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `created_at`         datetime(6) DEFAULT NULL,
    `updated_at`         datetime(6) DEFAULT NULL,
    `customer_left`      tinyint(1) NOT NULL DEFAULT '0',
    `groomer_left`       tinyint(1) NOT NULL DEFAULT '0',
    `estimate_id`        bigint DEFAULT NULL,
    `groomer_profile_id` bigint DEFAULT NULL,
    `user_id`            bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FK4ruoa0svahhgljh104gntn3cy` (`estimate_id`),
    KEY                  `FKerwd4yj4h833il9cwprdul7pc` (`groomer_profile_id`),
    KEY                  `FKjqx1ixf0jep8hhi0jxhak9jor` (`user_id`),
    CONSTRAINT `FK4ruoa0svahhgljh104gntn3cy` FOREIGN KEY (`estimate_id`) REFERENCES `estimate` (`id`),
    CONSTRAINT `FKerwd4yj4h833il9cwprdul7pc` FOREIGN KEY (`groomer_profile_id`) REFERENCES `groomer_profile` (`id`),
    CONSTRAINT `FKjqx1ixf0jep8hhi0jxhak9jor` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `chat_message`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `created_at`   datetime(6) DEFAULT NULL,
    `updated_at`   datetime(6) DEFAULT NULL,
    `image_key`    varchar(255) DEFAULT NULL,
    `message_text` text,
    `send_at`      datetime(6) DEFAULT NULL,
    `sender_id`    bigint       DEFAULT NULL,
    `sender_role`  enum('CUSTOMER','GROOMER') DEFAULT NULL,
    `room_id`      bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY            `FKfvbc4wvhk51y0qtnjrbminxfu` (`room_id`),
    CONSTRAINT `FKfvbc4wvhk51y0qtnjrbminxfu` FOREIGN KEY (`room_id`) REFERENCES `chat_room` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orders`
(
    `id`           bigint NOT NULL AUTO_INCREMENT,
    `created_at`   datetime(6) DEFAULT NULL,
    `updated_at`   datetime(6) DEFAULT NULL,
    `amount_value` int          DEFAULT NULL,
    `order_name`   varchar(255) DEFAULT NULL,
    `status`       enum('ACCEPTED','REJECTED') DEFAULT NULL,
    `estimate_id`  bigint       DEFAULT NULL,
    `user_id`      bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKe3fced7bnhi5eenfipqv4a4pf` (`estimate_id`),
    KEY            `FKel9kyl84ego2otj2accfd8mr7` (`user_id`),
    CONSTRAINT `FKel9kyl84ego2otj2accfd8mr7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FKl067d7tt15175oe1ft9fffrsa` FOREIGN KEY (`estimate_id`) REFERENCES `estimate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment`
(
    `id`             bigint NOT NULL AUTO_INCREMENT,
    `created_at`     datetime(6) DEFAULT NULL,
    `updated_at`     datetime(6) DEFAULT NULL,
    `payment_key`    varchar(255) DEFAULT NULL,
    `payment_method` varchar(255) DEFAULT NULL,
    `payment_status` enum('ACCEPTED','CANCELED','REJECTED') DEFAULT NULL,
    `requested_at`   datetime(6) DEFAULT NULL,
    `total_amount`   int          DEFAULT NULL,
    `order_id`       bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKmf7n8wo2rwrxsd6f3t9ub2mep` (`order_id`),
    CONSTRAINT `FKlouu98csyullos9k25tbpk4va` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coupon`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `coupon_name`     varchar(255) DEFAULT NULL,
    `discount_amount` int    NOT NULL,
    `discount_type`   enum('FIXED','RATE') DEFAULT NULL,
    `expired_at`      datetime(6) DEFAULT NULL,
    `status`          enum('EXPIRED','NOT_USED','USED') DEFAULT NULL,
    `user_id`         bigint NOT NULL,
    PRIMARY KEY (`id`),
    KEY               `FKmfuic7ht7p0xvyoxhq9oydhal` (`user_id`),
    CONSTRAINT `FKmfuic7ht7p0xvyoxhq9oydhal` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coupon_event`
(
    `id`              bigint NOT NULL AUTO_INCREMENT,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `discount_amount` int    NOT NULL,
    `discount_type`   enum('FIXED','RATE') DEFAULT NULL,
    `ended_at`        datetime(6) DEFAULT NULL,
    `name`            varchar(255) DEFAULT NULL,
    `remain_quantity` int    NOT NULL,
    `started_at`      datetime(6) DEFAULT NULL,
    `status`          enum('ACTIVE','EXPIRED') DEFAULT NULL,
    `total_quantity`  int    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `dog_profile_feature`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `profile_id` bigint DEFAULT NULL,
    `feature_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKkihgcp9b7kdkw8wb0fevl5oy6` (`profile_id`),
    KEY          `FK7cgtnvgje5jecdahmbl3lxdfa` (`feature_id`),
    CONSTRAINT `FKkihgcp9b7kdkw8wb0fevl5oy6` FOREIGN KEY (`profile_id`) REFERENCES `dog_profile` (`id`),
    CONSTRAINT `FK7cgtnvgje5jecdahmbl3lxdfa` FOREIGN KEY (`feature_id`) REFERENCES `feature` (`feature_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification`
(
    `notification_id` bigint NOT NULL AUTO_INCREMENT,
    `created_at`      datetime(6) DEFAULT NULL,
    `updated_at`      datetime(6) DEFAULT NULL,
    `description`     varchar(255) DEFAULT NULL,
    `name`            varchar(255) DEFAULT NULL,
    `notice_at`       datetime(6) DEFAULT NULL,
    `status`          enum('NOT_READ','READ') DEFAULT NULL,
    `user_id`         bigint       DEFAULT NULL,
    PRIMARY KEY (`notification_id`),
    KEY               `FKb0yvoep4h4k92ipon31wmdf7e` (`user_id`),
    CONSTRAINT `FKb0yvoep4h4k92ipon31wmdf7e` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `estimate_request_profiles`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `profile_id` bigint DEFAULT NULL,
    `request_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FK2tgfb3gq276pmcrworbng1mt0` (`profile_id`),
    KEY          `FK1qreqf69emwkruacrrxpf6kds` (`request_id`),
    CONSTRAINT `FK1qreqf69emwkruacrrxpf6kds` FOREIGN KEY (`request_id`) REFERENCES `estimate_request` (`id`),
    CONSTRAINT `FK2tgfb3gq276pmcrworbng1mt0` FOREIGN KEY (`profile_id`) REFERENCES `dog_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `estimate_request_service`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `request_id` bigint DEFAULT NULL,
    `service_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY          `FKoam8pap713fsbclf3i4mweol3` (`request_id`),
    KEY          `FK1a4xnm7amnqay0je23kruq2r4` (`service_id`),
    CONSTRAINT `FK1a4xnm7amnqay0je23kruq2r4` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
    CONSTRAINT `FKoam8pap713fsbclf3i4mweol3` FOREIGN KEY (`request_id`) REFERENCES `estimate_request` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `estimate_service_price`
(
    `price_id`    bigint NOT NULL AUTO_INCREMENT,
    `created_at`  datetime(6) DEFAULT NULL,
    `updated_at`  datetime(6) DEFAULT NULL,
    `price`       int    NOT NULL,
    `estimate_id` bigint DEFAULT NULL,
    `service_id`  bigint DEFAULT NULL,
    PRIMARY KEY (`price_id`),
    KEY           `FKi0xoqif877y87bxa4l2ixa7uv` (`estimate_id`),
    KEY           `FKbuomfou3jnqd9kv4qst867oap` (`service_id`),
    CONSTRAINT `FKbuomfou3jnqd9kv4qst867oap` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`),
    CONSTRAINT `FKi0xoqif877y87bxa4l2ixa7uv` FOREIGN KEY (`estimate_id`) REFERENCES `estimate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `review`
(
    `id`                 bigint NOT NULL AUTO_INCREMENT,
    `created_at`         datetime(6) DEFAULT NULL,
    `updated_at`         datetime(6) DEFAULT NULL,
    `image_key`          varchar(255) DEFAULT NULL,
    `star_score` double NOT NULL,
    `text`               text,
    `groomer_profile_id` bigint       DEFAULT NULL,
    `user_id`            bigint       DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                  `FK6f7aypfjoib9dvoni6cy6wc32` (`groomer_profile_id`),
    KEY                  `FKiyf57dy48lyiftdrf7y87rnxi` (`user_id`),
    CONSTRAINT `FK6f7aypfjoib9dvoni6cy6wc32` FOREIGN KEY (`groomer_profile_id`) REFERENCES `groomer_profile` (`id`),
    CONSTRAINT `FKiyf57dy48lyiftdrf7y87rnxi` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;