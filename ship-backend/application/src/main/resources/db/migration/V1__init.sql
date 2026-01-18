CREATE SEQUENCE IF NOT EXISTS cargos_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS catains_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS shippings_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS ships_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE cargos
(
    id           BIGINT NOT NULL,
    cargo_id     UUID,
    cargo_name   VARCHAR(255),
    cargo_weight FLOAT,
    CONSTRAINT pk_cargos PRIMARY KEY (id)
);

CREATE TABLE catains
(
    id              BIGINT NOT NULL,
    catain_id       UUID,
    catain_name     VARCHAR(255),
    catain_image_id VARCHAR(255),
    CONSTRAINT pk_catains PRIMARY KEY (id)
);

CREATE TABLE quotes
(
    id    BIGINT NOT NULL,
    quote VARCHAR(255),
    CONSTRAINT pk_quotes PRIMARY KEY (id)
);

CREATE TABLE shipping_outbox
(
    message_id     UUID NOT NULL,
    aggregate_type VARCHAR(255),
    aggregate_id   UUID,
    event_type     VARCHAR(255),
    payload        TEXT,
    CONSTRAINT pk_shipping_outbox PRIMARY KEY (message_id)
);

CREATE TABLE shippings
(
    id             BIGINT NOT NULL,
    shipping_id    UUID,
    sailors_code   VARCHAR(255),
    ship_id        BIGINT NOT NULL,
    shipping_state VARCHAR(255),
    CONSTRAINT pk_shippings PRIMARY KEY (id)
);

CREATE TABLE ships
(
    id        BIGINT NOT NULL,
    ship_id   UUID,
    ship_name VARCHAR(255),
    catain_id BIGINT,
    CONSTRAINT pk_ships PRIMARY KEY (id)
);

CREATE TABLE ships_cargos
(
    cargo_id BIGINT NOT NULL,
    ship_id  BIGINT NOT NULL
);

ALTER TABLE shippings
    ADD CONSTRAINT FK_SHIPPINGS_ON_SHIP FOREIGN KEY (ship_id) REFERENCES ships (id);

ALTER TABLE ships
    ADD CONSTRAINT FK_SHIPS_ON_CATAIN FOREIGN KEY (catain_id) REFERENCES catains (id);

ALTER TABLE ships_cargos
    ADD CONSTRAINT fk_shicar_on_cargo_persistence_entity FOREIGN KEY (cargo_id) REFERENCES cargos (id);

ALTER TABLE ships_cargos
    ADD CONSTRAINT fk_shicar_on_shipping_persistence_entity FOREIGN KEY (ship_id) REFERENCES shippings (id);