-- removes clear duplicates from the place table, run it after import_places.sql
-- preview_duplicates.sql shows what this would remove, without removing anything
-- the two files use the same rules, so if you change one, change the other too
-- run with: Get-Content scripts\clean_places.sql | docker exec -i pourplate-postgres psql -U pourplate -d tasting

WITH pairs AS (
    -- candidates that are close to each other, uses the index on latitude and longitude
    SELECT a.id AS a_id, b.id AS b_id
    FROM place a
             JOIN place b ON b.latitude BETWEEN a.latitude - 0.001 AND a.latitude + 0.001
        AND b.longitude BETWEEN a.longitude - 0.002 AND a.longitude + 0.002
        AND a.id <> b.id
    UNION
    -- candidates at the same address, even if their pins are far apart
    SELECT a.id, b.id
    FROM place a
             JOIN place b ON a.address = b.address AND a.city = b.city
        AND a.address <> '' AND a.id <> b.id
),
     core AS (
         -- the name without words that many places have, so "Restaurang Hemma" and "Hemma" become the same
         SELECT id, confidence,
                trim(regexp_replace(
                        regexp_replace(lower(name), '\m(restaurang|restaurant|pizzeria|pizza|café|cafe|kafé|bar|bistro|grill|och|and|ab|nya|the)\M', ' ', 'g'),
                        '[^a-z0-9åäöéü ]', ' ', 'g')) AS core_name
         FROM place
     ),
     to_remove AS (
         -- the weaker copy of every duplicate pair, a place can be the weaker copy in more than one pair
         SELECT DISTINCT a.id
         FROM pairs p
                  JOIN core a ON a.id = p.a_id
                  JOIN core b ON b.id = p.b_id
         WHERE a.core_name <> ''
           AND similarity(a.core_name, b.core_name) >= 0.85
           AND (a.confidence < b.confidence OR (a.confidence = b.confidence AND a.id > b.id))
           -- never remove a place that someone has reviewed
           AND NOT EXISTS (SELECT 1 FROM restaurant r WHERE r.place_id = a.id)
     )
DELETE FROM place
WHERE id IN (SELECT id FROM to_remove);