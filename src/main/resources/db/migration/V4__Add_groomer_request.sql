CREATE TABLE `groomer_estimate_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `groomer_request_status` ENUM('PAID', 'PENDING', 'CANCEL', 'COMPLETED'),
    `request_id` BIGINT NOT NULL,
    `groomer_profile_id` BIGINT NOT NULL,
    PRIMARY KEY (`id`),
    FOREIGN KEY (`request_id`) REFERENCES `estimate_request` (`id`),
    FOREIGN KEY (`groomer_profile_id`) REFERENCES `groomer_profile` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
