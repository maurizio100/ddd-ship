-- Every ship has a Home Harbor (STORY-027): the Harbor where it was registered. It never changes and travels
-- with the ship in shipping-published, so the application writes it for every new or arriving ship.
-- Backfill of existing rows: a ship registered here (ship_arrived_from NULL) gets this Harbor; an arrived ship
-- gets the Harbor it came from, the best guess the data allows (exact for a single voyage).
-- This Harbor's name comes from the Flyway placeholder harbor_name (spring.flyway.placeholders.harbor_name,
-- set from harbor.name). It is dollar-quoted, so a Harbor Name containing a quote cannot break the statement.
-- Flyway does not replace a placeholder that directly follows a dollar sign, so the quoted value starts with
-- one space, which substr removes. A blank name leaves the column NULL, so SET NOT NULL fails the migration
-- rather than giving registered ships an empty Home Harbor.
ALTER TABLE ships
    ADD COLUMN ship_home_harbor VARCHAR(255);

UPDATE ships
SET ship_home_harbor = COALESCE(ship_arrived_from, NULLIF(substr($harbor$ ${harbor_name}$harbor$, 2), ''));

ALTER TABLE ships
    ALTER COLUMN ship_home_harbor SET NOT NULL;
