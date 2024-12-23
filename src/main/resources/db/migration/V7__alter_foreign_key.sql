ALTER TABLE district_service DROP
    FOREIGN KEY FKnyllignrxg8cu6582avprepnt;
ALTER TABLE district_service
    ADD CONSTRAINT FKnyllignrxg8cu6582avprepnt
        FOREIGN KEY (profile_id) REFERENCES groomer_profile(id);

ALTER TABLE orders
    DROP FOREIGN KEY FKl067d7tt15175oe1ft9fffrsa;
ALTER TABLE orders
    ADD CONSTRAINT FKl067d7tt15175oe1ft9fffrsa
        FOREIGN KEY (`estimate_id`) REFERENCES `estimate` (`id`) ON DELETE CASCADE;

ALTER TABLE groomer_badge
    DROP FOREIGN KEY FKhiijgrbrxnsvo4pbgqb72r97r;

ALTER TABLE groomer_badge
    ADD CONSTRAINT FKhiijgrbrxnsvo4pbgqb72r97r
        FOREIGN KEY (profile_id) REFERENCES groomer_profile(id) ON DELETE CASCADE;