create table public.cargos
(
    id           bigint not null
        primary key,
    cargo_id     uuid,
    cargo_name   varchar(255),
    cargo_weight real
);

alter table public.cargos
    owner to hexagonship_user;

create table public.catains
(
    id              bigint not null
        primary key,
    catain_id       uuid,
    catain_image_id varchar(255),
    catain_name     varchar(255)
);

alter table public.catains
    owner to hexagonship_user;

create table public.quotes
(
    id    bigint not null
        primary key,
    quote varchar(255)
);

alter table public.quotes
    owner to hexagonship_user;

create table public.shipping_outbox
(
    message_id     uuid not null
        primary key,
    aggregate_id   uuid,
    aggregate_type varchar(255),
    payload        text,
    event_type     varchar(255)
);

alter table public.shipping_outbox
    owner to hexagonship_user;

create table public.ships
(
    id        bigint not null
        primary key,
    ship_id   uuid,
    ship_name varchar(255),
    catain_id bigint
        constraint fk5j921nv4u39mp2ocafkv5bylj
            references public.catains
);

alter table public.ships
    owner to hexagonship_user;

create table public.shippings
(
    id             bigint not null
        primary key,
    sailors_code   varchar(255),
    shipping_id    uuid,
    shipping_state varchar(255)
        constraint shippings_shipping_state_check
            check ((shipping_state)::text = ANY
        ((ARRAY ['PREPARING'::character varying, 'SHIPPING'::character varying, 'DONE'::character varying])::text[])),
    ship_id        bigint not null
        constraint fke6c17qeaju12p58dn3v8u0pcd
            references public.ships
);

alter table public.shippings
    owner to hexagonship_user;

create table public.ships_cargos
(
    ship_id  bigint not null
        constraint fk8fo89h3nh9ubucagstjuanulr
            references public.shippings,
    cargo_id bigint not null
        constraint fk9ak91iaklrlrghltbu5tydnym
            references public.cargos
);

alter table public.ships_cargos
    owner to hexagonship_user;

