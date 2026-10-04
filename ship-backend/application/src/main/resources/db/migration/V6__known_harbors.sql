-- The Known Harbors of this Harbor (ADR-0005): every other Harbor it has learned of from Harbor Opened.
-- The unique Harbor Name makes learning idempotent: a re-opening Harbor is known only once.
CREATE SEQUENCE IF NOT EXISTS known_harbors_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE known_harbors
(
    id          BIGINT                   NOT NULL,
    harbor_name VARCHAR(255)             NOT NULL,
    learned_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_known_harbors PRIMARY KEY (id),
    CONSTRAINT uq_known_harbors_harbor_name UNIQUE (harbor_name)
);
