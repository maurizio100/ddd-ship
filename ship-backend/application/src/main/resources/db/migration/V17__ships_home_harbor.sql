-- Every ship has a Home Harbor (STORY-027): the Harbor where it was registered. It never changes and travels
-- with the ship in shipping-published, so the application writes it for every new or arriving ship.
-- Backfill of existing rows: a ship registered here (ship_arrived_from NULL) gets this Harbor; an arrived ship
-- gets the Harbor it came from, the best guess the data allows (exact for a single voyage).
-- This Harbor's name comes from the Flyway placeholder harbor_name (spring.flyway.placeholders.harbor_name,
-- set from harbor.name). It is dollar-quoted, so a Harbor Name containing a quote cannot break the statement.
ALTER TABLE ships
    ADD COLUMN ship_home_harbor VARCHAR(255);

UPDATE ships
SET ship_home_harbor = COALESCE(ship_arrived_from, $harbor$${harbor_name}$harbor$);

ALTER TABLE ships
    ALTER COLUMN ship_home_harbor SET NOT NULL;
