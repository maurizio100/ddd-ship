-- A ship has a Ship Class (SLOOP, BRIG or GALLEON); every ship that exists before this migration becomes a Sloop.
ALTER TABLE ships
    ADD COLUMN ship_class VARCHAR(16) NOT NULL DEFAULT 'SLOOP',
    ADD CONSTRAINT ck_ships_ship_class CHECK (ship_class IN ('SLOOP', 'BRIG', 'GALLEON'));
