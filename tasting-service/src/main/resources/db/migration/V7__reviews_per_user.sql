-- V7: users and their reviews, second stage (contract)
-- the java code now always sets user_id, so the database can require it
ALTER TABLE restaurant ALTER COLUMN user_id SET NOT NULL;

-- one review per place becomes one review per place per person
ALTER TABLE restaurant DROP CONSTRAINT restaurant_osm_id_key;
ALTER TABLE restaurant ADD CONSTRAINT restaurant_user_place_unique UNIQUE (user_id, place_id);

-- the new unique rule also works as an index that starts with user_id
-- so the separate index from V6 is no longer needed
DROP INDEX idx_restaurant_user;