-- Fleet membership (STORY-007): a ship leaves this Harbor's fleet when its Origin Harbor learns of its
-- Arrival elsewhere (Ship Arrived). Its row and its Shippings are kept as history, never deleted. A ship
-- with the same Ship Id that arrives here again is taken back in by setting the flag again.
ALTER TABLE ships
    ADD COLUMN ship_in_fleet BOOLEAN NOT NULL DEFAULT TRUE;
