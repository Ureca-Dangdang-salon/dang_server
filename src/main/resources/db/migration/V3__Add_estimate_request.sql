ALTER TABLE `review`
DROP
COLUMN `image_key`;

ALTER TABLE `estimate`
    ADD COLUMN `total_amount` INT;

ALTER TABLE `estimate_request`
DROP
COLUMN `current_photo_key`,
    DROP
COLUMN `style_ref_photo_key`,
    DROP
COLUMN `aggression`,
    DROP
COLUMN `health_issue`;

ALTER TABLE `estimate_request_profiles`
    ADD COLUMN `current_photo_key` VARCHAR(255),
    ADD COLUMN `style_ref_photo_key` VARCHAR(255),
    ADD COLUMN `aggression` BIT(1),
    ADD COLUMN `health_issue` BIT(1),
    ADD COLUMN `description` TEXT;