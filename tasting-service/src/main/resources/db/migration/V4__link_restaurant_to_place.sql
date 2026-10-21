-- V4: my reviews now point to a place from the imported place table instead of openstreetmap
-- no foreign key on purpose: the import deletes and refills place, and my reviews must survive that
ALTER TABLE restaurant RENAME COLUMN osm_id TO place_id;