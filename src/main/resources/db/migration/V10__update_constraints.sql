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

ALTER TABLE estimate_request_profiles DROP FOREIGN KEY FK2tgfb3gq276pmcrworbng1mt0;
ALTER TABLE estimate_request_profiles ADD CONSTRAINT FK2tgfb3gq276pmcrworbng1mt0
    FOREIGN KEY (profile_id) REFERENCES dog_profile(id) ON DELETE CASCADE;

ALTER TABLE estimate_request_service
DROP FOREIGN KEY estimate_request_service_ibfk_1;

ALTER TABLE estimate_request_service
    ADD CONSTRAINT estimate_request_service_ibfk_1
        FOREIGN KEY (request_id) REFERENCES estimate_request_profiles(id) ON DELETE CASCADE;

ALTER TABLE payment
DROP FOREIGN KEY FKlouu98csyullos9k25tbpk4va;

ALTER TABLE payment
    ADD CONSTRAINT FKlouu98csyullos9k25tbpk4va
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

ALTER TABLE estimate DROP FOREIGN KEY FKr2cdpc6qr1bqa8km6na79v9qs;
ALTER TABLE estimate
    ADD CONSTRAINT FKr2cdpc6qr1bqa8km6na79v9qs
        FOREIGN KEY (request_id) REFERENCES estimate_request (id) ON DELETE CASCADE;

ALTER TABLE groomer_estimate_request
DROP FOREIGN KEY groomer_estimate_request_ibfk_1;

ALTER TABLE groomer_estimate_request
    ADD CONSTRAINT groomer_estimate_request_ibfk_1
        FOREIGN KEY (request_id) REFERENCES estimate_request (id)
            ON DELETE CASCADE;

ALTER TABLE fcm_token
DROP FOREIGN KEY fcm_token_ibfk_1;
ALTER TABLE fcm_token
    ADD CONSTRAINT FK_fcm_token_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

