-- The Arrivals this Harbor has handled (STORY-007 review), keyed by the Origin Harbor's Shipping id. A
-- re-published Shipping Published arrives under a new event id, so the inbox cannot drop it; this table
-- does, even after the ship has sailed on and left the fleet. Written in the same transaction as the
-- Arrival. The Shipping id is the primary key on purpose: it is the idempotency guarantee.
CREATE TABLE arrivals
(
    shipping_id UUID                     NOT NULL,
    ship_id     UUID                     NOT NULL,
    arrived_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_arrivals PRIMARY KEY (shipping_id)
);
