-- The Prices of this Harbor: what each Cargo is worth here. One set of Prices per Harbor database (ADR-0003).
-- No rows are seeded: the Harbor Opening rolls a Price for every Cargo that has none
-- (INSERT … ON CONFLICT (cargo_id) DO NOTHING), so the first Price wins and is never re-rolled.
CREATE SEQUENCE IF NOT EXISTS prices_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE prices
(
    id           BIGINT        NOT NULL,
    cargo_id     BIGINT        NOT NULL,
    price_amount NUMERIC(8, 2) NOT NULL,
    CONSTRAINT pk_prices PRIMARY KEY (id),
    CONSTRAINT fk_prices_on_cargos FOREIGN KEY (cargo_id) REFERENCES cargos (id),
    CONSTRAINT uq_prices_cargo_id UNIQUE (cargo_id),
    CONSTRAINT ck_prices_price_amount_not_negative CHECK (price_amount >= 0)
);

-- The Savings of this Harbor: the money it holds, exact to the cent (ADR-0008). One row; never below 0.
CREATE SEQUENCE IF NOT EXISTS savings_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE savings
(
    id             BIGINT         NOT NULL,
    savings_amount NUMERIC(12, 2) NOT NULL,
    CONSTRAINT pk_savings PRIMARY KEY (id),
    CONSTRAINT ck_savings_savings_amount_not_negative CHECK (savings_amount >= 0)
);

-- The Starting Savings: 1000.00 $. Flyway applies this once per database, i.e. once when the
-- Harbor opens for the first time, and never again on a restart.
INSERT INTO savings (id, savings_amount)
VALUES (nextval('savings_seq'), 1000.00);
