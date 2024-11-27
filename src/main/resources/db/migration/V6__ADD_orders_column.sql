ALTER TABLE orders ADD COLUMN `toss_order_id` VARCHAR(255);

ALTER TABLE orders DROP COLUMN `status`;

ALTER TABLE orders ADD COLUMN `status` ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT NULL;

ALTER TABLE dog_profile DROP COLUMN `year`;
ALTER TABLE dog_profile DROP COLUMN `month`;

ALTER TABLE dog_profile ADD COLUMN `dog_year` INT;
ALTER TABLE dog_profile ADD COLUMN `dog_month` INT;
