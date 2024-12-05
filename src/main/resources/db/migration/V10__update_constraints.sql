ALTER TABLE dog_profile DROP FOREIGN KEY FK8dv8uxmiead9ibrmomyq9ntp7;
ALTER TABLE dog_profile ADD CONSTRAINT FK_dog_profile_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE orders DROP FOREIGN KEY FKel9kyl84ego2otj2accfd8mr7;
ALTER TABLE orders ADD CONSTRAINT FK_orders_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE review DROP FOREIGN KEY FKiyf57dy48lyiftdrf7y87rnxi;
ALTER TABLE review ADD CONSTRAINT FK_review_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE estimate_request DROP FOREIGN KEY FKjmveywmjo8qkp6q0af9uvp57v;
ALTER TABLE estimate_request ADD CONSTRAINT FK_estimate_request_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE coupon DROP FOREIGN KEY FKmfuic7ht7p0xvyoxhq9oydhal;
ALTER TABLE coupon ADD CONSTRAINT FK_coupon_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE groomer_profile DROP FOREIGN KEY FKgc57p2busxulf0egn8knl2h1s;
ALTER TABLE groomer_profile ADD CONSTRAINT FK_groomer_profile_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

