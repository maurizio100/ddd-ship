-- The Recruit Pool of this Harbor (ADR-0009). No rows are seeded: the Harbor Opening fills the pool at runtime
-- from the Harbor Name (INSERT … ON CONFLICT (ordinal) DO NOTHING), so two Harbors offer different Recruits.
-- The ordinal numbers the Recruits ever generated here and is never reused.
CREATE SEQUENCE IF NOT EXISTS recruits_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE recruits
(
    id         BIGINT      NOT NULL,
    recruit_id UUID        NOT NULL,
    ordinal    INT         NOT NULL,
    name       VARCHAR(64) NOT NULL,
    trait      VARCHAR(64) NOT NULL,
    CONSTRAINT pk_recruits PRIMARY KEY (id),
    CONSTRAINT uq_recruits_recruit_id UNIQUE (recruit_id),
    CONSTRAINT uq_recruits_ordinal UNIQUE (ordinal),
    CONSTRAINT ck_recruits_ordinal_not_negative CHECK (ordinal >= 0)
);
