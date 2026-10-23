-- merges openstreetmap places into the place table, run it after import_osm.sql
-- 1: links overture places to their matching osm place, and fills in a missing website or phone from osm
-- 2: adds osm places that overture does not have, so fewer places are missing
-- run with: Get-Content scripts\merge_osm.sql | docker exec -i pourplate-postgres psql -U pourplate -d tasting

-- start fresh: remove osm places from the last run, except ones someone has reviewed
DELETE FROM place p
WHERE p.source = 'osm'
  AND NOT EXISTS (SELECT 1 FROM restaurant r WHERE r.place_id = p.id);

UPDATE place SET osm_id = NULL WHERE source = 'overture';

-- 1: link each osm place to the overture place it matches best
WITH candidates AS (
    SELECT o.id AS osm_id, p.id AS place_id,
           similarity(core_name(o.name), core_name(p.name)) AS score
    FROM osm_place o
             JOIN place p ON p.latitude BETWEEN o.latitude - 0.001 AND o.latitude + 0.001
        AND p.longitude BETWEEN o.longitude - 0.002 AND o.longitude + 0.002
        AND p.source = 'overture'
    WHERE similarity(core_name(o.name), core_name(p.name)) >= 0.5
),
     best_for_osm AS (
         -- the best overture place for each osm place
         SELECT *, ROW_NUMBER() OVER (PARTITION BY osm_id ORDER BY score DESC) AS rank
         FROM candidates
     ),
     best_pairs AS (
         -- if two osm places picked the same overture place, keep only the better one
         SELECT osm_id, place_id,
                ROW_NUMBER() OVER (PARTITION BY place_id ORDER BY score DESC) AS rank
         FROM best_for_osm
         WHERE rank = 1
     )
UPDATE place p
SET osm_id  = o.id,
    website = COALESCE(p.website, o.website),
    phone   = COALESCE(p.phone, o.phone)
    FROM best_pairs b
JOIN osm_place o ON o.id = b.osm_id
WHERE p.id = b.place_id
  AND b.rank = 1;

-- 2: add osm places that no overture place was linked to
-- osm only says restaurant plus a cuisine like thai, so we build a category like thai_restaurant
INSERT INTO place (id, name, category, address, city, latitude, longitude, website, phone, source)
SELECT o.id,
       o.name,
       CASE
           WHEN o.amenity = 'cafe'       THEN 'cafe'
           WHEN o.amenity = 'pub'        THEN 'pub'
           WHEN o.amenity = 'bar'        THEN 'bar'
           WHEN o.amenity = 'biergarten' THEN 'beer_garden'
           -- the first cuisine if there are several, like "pizza;kebab" becomes pizza_restaurant
           WHEN o.cuisine IS NOT NULL    THEN lower(split_part(o.cuisine, ';', 1)) || '_restaurant'
           WHEN o.amenity = 'fast_food'  THEN 'fast_food_restaurant'
           ELSE 'restaurant'
           END,
       o.address,
       o.city,
       o.latitude,
       o.longitude,
       o.website,
       o.phone,
       'osm'
FROM osm_place o
WHERE NOT EXISTS (SELECT 1 FROM place p WHERE p.osm_id = o.id)
-- a reviewed osm place from the last run is still there, so skip it instead of failing
    ON CONFLICT (id) DO NOTHING;

-- how it looks now
SELECT source,
       COUNT(*)                               AS places,
       COUNT(*) FILTER (WHERE osm_id IS NOT NULL) AS confirmed_by_osm
FROM place
GROUP BY source;