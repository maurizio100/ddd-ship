-- Every ship carries Earnings (STORY-049): the Delivery Prices paid for its Cargo away from its Home Harbor.
-- Money is exact (ADR-0008): NUMERIC with two decimals, never a float. Existing ships carry none.
ALTER TABLE ships
    ADD COLUMN ship_earnings NUMERIC(12, 2) NOT NULL DEFAULT 0,
    ADD CONSTRAINT ck_ships_ship_earnings_not_negative CHECK (ship_earnings >= 0);
