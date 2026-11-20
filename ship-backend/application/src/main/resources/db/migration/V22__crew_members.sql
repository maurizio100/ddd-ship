-- The Crew aboard a ship (STORY-034). A Crew Member keeps the UUID of the Recruit it was Hired as, so a Crew Member
-- exists at most once. Written only by a Hire, in the transaction that pays the Hiring Fee.
CREATE SEQUENCE IF NOT EXISTS crew_members_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE crew_members
(
    id                BIGINT      NOT NULL,
    crew_member_id    UUID        NOT NULL,
    crew_member_name  VARCHAR(64) NOT NULL,
    crew_member_trait VARCHAR(64) NOT NULL,
    ship_id           BIGINT      NOT NULL,
    CONSTRAINT pk_crew_members PRIMARY KEY (id),
    CONSTRAINT uq_crew_members_crew_member_id UNIQUE (crew_member_id),
    CONSTRAINT fk_crew_members_on_ships FOREIGN KEY (ship_id) REFERENCES ships (id)
);

-- A Hired Recruit is marked, never deleted, so its ordinal is never generated again.
ALTER TABLE recruits ADD COLUMN recruit_hired BOOLEAN NOT NULL DEFAULT false;
