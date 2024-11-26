ALTER TABLE orders ADD COLUMN `toss_order_id` varchar(255) ;

ALTER TABLE orders DROP COLUMN `status`;

ALTER TABLE orders ADD COLUMN `status` enum('PENDING','ACCEPTED','REJECTED') DEFAULT NULL;