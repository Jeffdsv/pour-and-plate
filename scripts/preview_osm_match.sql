-- shows how openstreetmap places match overture places, without changing anything
-- run with: Get-Content scripts\preview_osm_match.sql | docker exec -i pourplate-postgres psql -U pourplate -d tasting

WITH candidates AS (
    -- every overture place close to an osm place, with how alike their names are
    SELECT o.id AS osm_id, o.name AS osm_name,
           p.id AS place_id, p.name AS place_name,
           similarity(core_name(o.name), core_name(p.name)) AS score
    FROM osm_place o
             JOIN place p ON p.latitude BETWEEN o.latitude - 0.001 AND o.latitude + 0.001
        AND p.longitude BETWEEN o.longitude - 0.002 AND o.longitude + 0.002
        AND p.source = 'overture'
),
     best AS (
         -- the best overture match for each osm place, numbered 1 for the highest score
         SELECT *, ROW_NUMBER() OVER (PARTITION BY osm_id ORDER BY score DESC) AS rank
         FROM candidates
         WHERE score >= 0.5
     )
SELECT
    (SELECT COUNT(*) FROM osm_place)                     AS osm_places,
    (SELECT COUNT(*) FROM best WHERE rank = 1)           AS matched_with_overture,
    (SELECT COUNT(*) FROM osm_place) - (SELECT COUNT(*) FROM best WHERE rank = 1) AS only_in_osm;

-- the weakest matches, to check that 0.5 is not too low
WITH candidates AS (
    SELECT o.name AS osm_name, p.name AS place_name,
           similarity(core_name(o.name), core_name(p.name)) AS score,
           ROW_NUMBER() OVER (PARTITION BY o.id ORDER BY similarity(core_name(o.name), core_name(p.name)) DESC) AS rank
    FROM osm_place o
             JOIN place p ON p.latitude BETWEEN o.latitude - 0.001 AND o.latitude + 0.001
        AND p.longitude BETWEEN o.longitude - 0.002 AND o.longitude + 0.002
        AND p.source = 'overture'
    WHERE similarity(core_name(o.name), core_name(p.name)) >= 0.5
)
SELECT osm_name, place_name, ROUND(score::numeric, 2) AS score
FROM candidates
WHERE rank = 1
ORDER BY score
    LIMIT 30;