-- Every table needs a replica identity: a publication that covers ships_cargos (such as Debezium's
-- default FOR ALL TABLES) makes PostgreSQL reject DELETE on a table without one. A ship's Loaded Cargo
-- never holds the same Cargo twice, so (ship_id, cargo_id) is the natural key.
ALTER TABLE ships_cargos ADD CONSTRAINT pk_ships_cargos PRIMARY KEY (ship_id, cargo_id);

-- Databases patched by hand with REPLICA IDENTITY FULL go back to the primary key, like a fresh one.
ALTER TABLE ships_cargos REPLICA IDENTITY DEFAULT;
