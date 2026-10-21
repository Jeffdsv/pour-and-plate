-- V5: overture ids are longer than openstreetmap ids, so make room for them
ALTER TABLE restaurant ALTER COLUMN place_id TYPE VARCHAR(64);