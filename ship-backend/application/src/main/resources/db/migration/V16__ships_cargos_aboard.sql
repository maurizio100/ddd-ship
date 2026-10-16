-- Cargo aboard a ship without a Shipping (an Incoming Ship, STORY-044): one row per Cargo instance, so the
-- same Cargo aboard twice is two rows. ships_cargos cannot hold it, since its ship_id references shippings(id)
-- and an arrived ship has no Shipping.
-- ships.ship_incoming marks an Incoming Ship. The table and the column are new, so existing ships have no
-- Cargo aboard and are not Incoming.
ALTER TABLE ships
    ADD COLUMN ship_incoming BOOLEAN NOT NULL DEFAULT false;

CREATE SEQUENCE IF NOT EXISTS ships_cargos_aboard_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE ships_cargos_aboard
(
    id       BIGINT NOT NULL,
    ship_id  BIGINT NOT NULL,
    cargo_id BIGINT NOT NULL,
    CONSTRAINT pk_ships_cargos_aboard PRIMARY KEY (id),
    CONSTRAINT fk_ships_cargos_aboard_on_ships FOREIGN KEY (ship_id) REFERENCES ships (id),
    CONSTRAINT fk_ships_cargos_aboard_on_cargos FOREIGN KEY (cargo_id) REFERENCES cargos (id)
);
