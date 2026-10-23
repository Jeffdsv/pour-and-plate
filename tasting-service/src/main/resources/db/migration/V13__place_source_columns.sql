-- more about each place, so we can work out kinds better and later merge with openstreetmap
-- hierarchy is overtures chain of categories as text, like food_and_drink,alcoholic_beverage_venue,bar,pub
ALTER TABLE place ADD COLUMN hierarchy TEXT;

-- how sure overture is that the place exists, from 0 to 1
ALTER TABLE place ADD COLUMN confidence DOUBLE PRECISION;

-- where the place comes from, overture now and osm later
ALTER TABLE place ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'overture';