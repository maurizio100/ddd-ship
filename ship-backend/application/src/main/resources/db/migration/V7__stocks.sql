-- The Stock of this Harbor: how many of each Cargo it has on hand. One Stock per Harbor database (ADR-0003).
-- The Stock never drops below 0; loading takes one out with a conditional UPDATE, unloading puts one back.
CREATE SEQUENCE IF NOT EXISTS stocks_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE stocks
(
    id             BIGINT  NOT NULL,
    cargo_id       BIGINT  NOT NULL,
    stock_quantity INTEGER NOT NULL,
    CONSTRAINT pk_stocks PRIMARY KEY (id),
    CONSTRAINT fk_stocks_on_cargos FOREIGN KEY (cargo_id) REFERENCES cargos (id),
    CONSTRAINT uq_stocks_cargo_id UNIQUE (cargo_id),
    CONSTRAINT ck_stocks_stock_quantity_not_negative CHECK (stock_quantity >= 0)
);

-- The Starting Stock: 3 of every Cargo. Flyway applies this once per database, i.e. once when the
-- Harbor opens for the first time, and never again on a restart.
INSERT INTO stocks (id, cargo_id, stock_quantity)
SELECT nextval('stocks_seq'), id, 3
FROM cargos;
