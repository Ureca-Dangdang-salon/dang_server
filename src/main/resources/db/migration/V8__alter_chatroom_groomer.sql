ALTER TABLE chat_room DROP FOREIGN KEY FKjqx1ixf0jep8hhi0jxhak9jor;
ALTER TABLE chat_room DROP FOREIGN KEY FKerwd4yj4h833il9cwprdul7pc;

ALTER TABLE chat_room DROP COLUMN user_id;
ALTER TABLE chat_room DROP COLUMN groomer_profile_id;

ALTER TABLE chat_room
    ADD COLUMN customer_id BIGINT NULL,
    ADD COLUMN groomer_id BIGINT NULL;

ALTER TABLE chat_room
    ADD CONSTRAINT fk_chatroom_customer_id FOREIGN KEY (customer_id) REFERENCES users(id),
    ADD CONSTRAINT fk_chatroom_groomer_id FOREIGN KEY (groomer_id) REFERENCES users(id);

