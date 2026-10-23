-- shows places that were probably replaced by another place at the same address, without changing anything
-- the pattern: an address has exactly two places of the same kind, one confirmed by osm and one not, with different names
-- run with: Get-Content scripts\preview_replaced.sql | docker exec -i pourplate-postgres psql -U pourplate -d tasting

WITH two_at_same_spot AS (
    -- addresses with exactly two places of the same kind, food halls with many places are left out
    SELECT address, city, kind
    FROM place
    WHERE address <> ''
    GROUP BY address, city, kind
    HAVING COUNT(*) = 2
),
     replaced AS (
         SELECT old.id, old.name AS probably_closed, new.name AS still_there, old.address, old.kind
         FROM place old
                  JOIN two_at_same_spot s ON s.address = old.address AND s.city = old.city AND s.kind = old.kind
                  JOIN place new ON new.address = old.address AND new.city = old.city
             AND new.kind = old.kind AND new.id <> old.id
         WHERE old.source = 'overture'
           AND old.osm_id IS NULL
           AND new.osm_id IS NOT NULL
           -- different names, so they are not just the same place written twice
           AND similarity(core_name(old.name), core_name(new.name)) < 0.5
           -- never touch a place someone has reviewed
           AND NOT EXISTS (SELECT 1 FROM restaurant r WHERE r.place_id = old.id)
     )
SELECT (SELECT COUNT(*) FROM replaced) AS total, probably_closed, still_there, address, kind
FROM replaced
ORDER BY kind, probably_closed
    LIMIT 40;