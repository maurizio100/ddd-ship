-- Ids of events consumed from other Harbors (ADR-0004). The event id is the primary key on purpose:
-- it is the idempotency guarantee, so there is no surrogate id or sequence.
CREATE TABLE inbox_events
(
    event_id    UUID                     NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_inbox_events PRIMARY KEY (event_id)
);
