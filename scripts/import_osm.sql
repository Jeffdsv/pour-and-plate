-- imports food and drink places from openstreetmap into the osm_place table
-- download first: curl.exe -L -o D:\osm\sweden-latest.osm.pbf https://download.geofabrik.de/europe/sweden-latest.osm.pbf
-- run from duckdb with: .read scripts/import_osm.sql

INSTALL spatial;
LOAD spatial;
INSTALL postgres;
LOAD postgres;

ATTACH IF NOT EXISTS 'host=localhost port=5432 dbname=tasting user=pourplate password=pourplate' AS pg (TYPE postgres);

-- the types of places we want, osm calls the type amenity
CREATE OR REPLACE MACRO is_food(tags) AS
    tags['amenity'] IN ('restaurant', 'cafe', 'bar', 'pub', 'biergarten', 'fast_food');

-- places drawn as a single point already have a position
CREATE OR REPLACE TABLE osm_points AS
SELECT 'osm:node/' || id AS id, tags, lat AS latitude, lon AS longitude
FROM ST_ReadOSM('D:/osm/sweden-latest.osm.pbf')
WHERE kind = 'node' AND is_food(tags) AND tags['name'] IS NOT NULL;

-- places drawn as a building outline only have a list of corner points, called refs
CREATE OR REPLACE TABLE osm_outlines AS
SELECT id, tags, refs
FROM ST_ReadOSM('D:/osm/sweden-latest.osm.pbf')
WHERE kind = 'way' AND is_food(tags) AND tags['name'] IS NOT NULL;

-- one row per corner, so we can look up where each corner is
CREATE OR REPLACE TABLE outline_corners AS
SELECT id AS way_id, UNNEST(refs) AS node_id
FROM osm_outlines;

-- the positions of only those corners, out of all the points in sweden
CREATE OR REPLACE TABLE corner_positions AS
SELECT id AS node_id, lat, lon
FROM ST_ReadOSM('D:/osm/sweden-latest.osm.pbf')
WHERE kind = 'node' AND id IN (SELECT node_id FROM outline_corners);

-- the middle of each building is the average of its corners
CREATE OR REPLACE TABLE osm_outline_centers AS
SELECT 'osm:way/' || o.id AS id, o.tags, AVG(p.lat) AS latitude, AVG(p.lon) AS longitude
FROM osm_outlines o
         JOIN outline_corners c ON c.way_id = o.id
         JOIN corner_positions p ON p.node_id = c.node_id
GROUP BY o.id, o.tags;

-- points and outlines together, with the fields we keep
CREATE OR REPLACE TABLE sweden_osm AS
SELECT id,
    left(tags['name'], 300)                                              AS name,
    tags['amenity']                                                      AS amenity,
    left(tags['cuisine'], 200)                                           AS cuisine,
    left(trim(concat_ws(' ', tags['addr:street'], tags['addr:housenumber'])), 300) AS address,
    left(tags['addr:city'], 100)                                         AS city,
    round(latitude, 6)                                                   AS latitude,
    round(longitude, 6)                                                  AS longitude,
    left(coalesce(tags['website'], tags['contact:website']), 500)        AS website,
    left(coalesce(tags['phone'], tags['contact:phone']), 50)             AS phone
FROM (
    SELECT id, tags, latitude, longitude FROM osm_points
    UNION ALL
    SELECT id, tags, latitude, longitude FROM osm_outline_centers
    );

-- replace the old osm places with the new ones
DELETE FROM pg.public.osm_place;

INSERT INTO pg.public.osm_place (id, name, amenity, cuisine, address, city, latitude, longitude, website, phone)
SELECT id, name, amenity, cuisine, nullif(address, ''), city, latitude, longitude, website, phone
FROM sweden_osm;

SELECT amenity, count(*) AS imported FROM sweden_osm GROUP BY amenity ORDER BY imported DESC;