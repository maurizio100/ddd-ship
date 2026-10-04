-- Every Harbor must name the same Cargo and the same Catain by the same id (STORY-006): an arriving
-- ship carries its Catain id and the ids of its Loaded Cargo from the Origin Harbor, and the
-- Destination Harbor resolves them in its own database. V2 and V3 seeded these business ids with
-- gen_random_uuid(), so each Harbor had different ones. This fixes them to the same literal UUIDs
-- everywhere. Foreign keys reference the BIGINT surrogate id, so no other row changes.

UPDATE cargos SET cargo_id = 'e1becd37-94bc-4b58-bd2b-bcbb63613669' WHERE id = 1; -- Ale
UPDATE cargos SET cargo_id = 'b3f84d5d-08b1-443d-aaef-bc3707a2adb3' WHERE id = 2; -- Chocolate
UPDATE cargos SET cargo_id = '2c1f9b41-6f81-41d9-8348-13429317bce7' WHERE id = 3; -- Cinnamon
UPDATE cargos SET cargo_id = 'fba46bd2-33bd-4a6d-9f5c-ec6685edb7a9' WHERE id = 4; -- Coffee
UPDATE cargos SET cargo_id = 'ee1f930a-8eb8-4f28-b8af-67a332f777f1' WHERE id = 5; -- Fruits
UPDATE cargos SET cargo_id = '2b38de1a-113f-4d66-bfe3-27e89d449ed1' WHERE id = 6; -- Leather
UPDATE cargos SET cargo_id = 'b31991d5-3fbf-4ca1-a032-0e9eec766e9a' WHERE id = 7; -- Paprika
UPDATE cargos SET cargo_id = '4f629046-b875-43ca-85a2-8f490f3fcb54' WHERE id = 8; -- Planks
UPDATE cargos SET cargo_id = 'dbc1c76c-ffd7-4cc6-b209-e64d1407d899' WHERE id = 9; -- Rum
UPDATE cargos SET cargo_id = '7bc669da-f68b-453a-837e-480390f148f4' WHERE id = 10; -- Silk
UPDATE cargos SET cargo_id = '0c63e166-3fa2-4118-85d1-6f514f091805' WHERE id = 11; -- Sugar
UPDATE cargos SET cargo_id = 'd8c8aeaf-fb65-40b3-ab87-5be3270d0a43' WHERE id = 12; -- Tobacco
UPDATE cargos SET cargo_id = '89bf7e52-6585-4ef7-b325-22d7ab247164' WHERE id = 13; -- Wheat
UPDATE cargos SET cargo_id = 'd863fc29-8f40-4208-b310-3972bf85ec79' WHERE id = 14; -- Wine

UPDATE catains SET catain_id = '4db95d01-a58a-4e34-88d0-cf2c1ccb0d91' WHERE id = 1; -- Furry Jones
UPDATE catains SET catain_id = '9bd9886b-3e77-44da-af95-4fe28e93ccad' WHERE id = 2; -- Bootsrap Bill
UPDATE catains SET catain_id = 'e07471dc-a193-4747-aee8-cb4512f8a450' WHERE id = 3; -- Catain Black Whiskers
UPDATE catains SET catain_id = '9c8f5d6e-8691-4da3-8553-1a0856e17825' WHERE id = 4; -- Catain Cat Sparrow
UPDATE catains SET catain_id = '475df0b9-7c53-4b02-93d8-bff646c50240' WHERE id = 5; -- Catain Purrbossa

ALTER TABLE cargos
    ADD CONSTRAINT uq_cargos_cargo_id UNIQUE (cargo_id);

ALTER TABLE catains
    ADD CONSTRAINT uq_catains_catain_id UNIQUE (catain_id);
