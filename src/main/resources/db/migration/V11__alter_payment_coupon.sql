ALTER TABLE payment
    ADD COLUMN coupon_id BIGINT;

ALTER TABLE payment
    ADD CONSTRAINT FK_payment_coupon
        FOREIGN KEY (coupon_id)
            REFERENCES coupon(id);
