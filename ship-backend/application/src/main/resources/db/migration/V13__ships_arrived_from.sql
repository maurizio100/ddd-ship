-- ship_arrived_from: the Origin Harbor (Harbor Name) of the Arrival that last took the ship into this
-- Harbor's fleet. NULL for a ship registered here and for ships that arrived before V13. Overwritten
-- on each Arrival that takes effect.
ALTER TABLE ships ADD COLUMN ship_arrived_from VARCHAR(255);
