CREATE TABLE `groomer_estimate_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `groomer_request_status` ENUM('PAID', 'PENDING', 'CANCEL', 'COMPLETED'),
    `request_id` BIGINT NOT NULL,
    `groomer_profile_id` BIGINT NOT NULL,
    `created_at`         datetime(6) DEFAULT NULL,
    `updated_at`         datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`request_id`) REFERENCES `estimate_request` (`id`),
    FOREIGN KEY (`groomer_profile_id`) REFERENCES `groomer_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE groomer_certification (
                                       `id`                 bigint NOT NULL AUTO_INCREMENT,
                                       `certification`      varchar(255) DEFAULT NULL,
                                       `profile_id`         bigint       DEFAULT NULL,
                                       `created_at`         datetime(6) DEFAULT NULL,
                                       `updated_at`         datetime(6) DEFAULT NULL,
                                       PRIMARY KEY (`id`),
                                       FOREIGN KEY (`profile_id`) REFERENCES groomer_profile (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `groomer_profile`
    DROP COLUMN `certification`;

ALTER TABLE `estimate`
    ADD COLUMN `date` datetime(6) DEFAULT NULL;

ALTER TABLE `estimate_request_service`
DROP FOREIGN KEY `FKoam8pap713fsbclf3i4mweol3`;

ALTER TABLE `estimate_request_service`
    ADD FOREIGN KEY (`request_id`) REFERENCES `estimate_request_profiles` (`id`);