-- marks places that were probably replaced by another place at the same address
-- the pattern: an address has exactly two places of the same kind, one confirmed by osm and one not, with different names
-- run it last, after import_places, clean_places, import_osm and merge_osm
-- run with: Get-Content scripts\mark_replaced.sql | docker exec -i pourplate-postgres psql -U pourplate -d tasting

-- start from scratch, so a place that is no longer in the pattern becomes visible again
UPDATE place SET likely_closed = false WHERE likely_closed;

WITH two_at_same_spot AS (
    -- addresses with exactly two places of the same kind, food halls with many places are left out
    SELECT address, city, kind
    FROM place
    WHERE address <> ''
    GROUP BY address, city, kind
    HAVING COUNT(*) = 2
),
     replaced AS (
         SELECT old.id
         FROM place old
                  JOIN two_at_same_spot s ON s.address = old.address AND s.city = old.city AND s.kind = old.kind
                  JOIN place new ON new.address = old.address AND new.city = old.city
             AND new.kind = old.kind AND new.id <> old.id
         WHERE old.source = 'overture'
           AND old.osm_id IS NULL
           AND new.osm_id IS NOT NULL
           AND similarity(core_name(old.name), core_name(new.name)) < 0.5
           -- never hide a place someone has reviewed
           AND NOT EXISTS (SELECT 1 FROM restaurant r WHERE r.place_id = old.id)
     )
UPDATE place
SET likely_closed = true
WHERE id IN (SELECT id FROM replaced);