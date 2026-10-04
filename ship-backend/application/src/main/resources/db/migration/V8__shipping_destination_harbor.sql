-- The Destination Harbor a Shipping was Released to (STORY-005): the Harbor Name of one of the Known
-- Harbors. Nullable, since a Shipping being prepared has none yet and Shippings Released before this
-- migration never named one.
ALTER TABLE shippings
    ADD COLUMN destination_harbor VARCHAR(255);
