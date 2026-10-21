-- imports all food and drink places in sweden from overture maps into my postgres
-- docker compose must be running
-- run from duckdb with: .read scripts/import_places.sql
-- running it again replaces all places with fresh data, my own reviews are never touched

INSTALL httpfs;
LOAD httpfs;
INSTALL postgres;
LOAD postgres;
SET s3_region = 'us-west-2';

-- connect to my local postgres from docker compose, skipped if already connected
ATTACH IF NOT EXISTS 'host=localhost port=5432 dbname=tasting user=pourplate password=pourplate' AS pg (TYPE postgres);

-- overture makes a new release every month, change the date below to get fresh data
-- the newest release is listed on https://stac.overturemaps.org
CREATE OR REPLACE TABLE sweden_places AS
SELECT
    id,
    left(names.primary, 300)          AS name,
    taxonomy.primary                  AS category,
    left(addresses[1].freeform, 300)  AS address,
    left(addresses[1].locality, 100)  AS city,
    round(bbox.ymin, 6)               AS latitude,
    round(bbox.xmin, 6)               AS longitude,
    left(websites[1], 500)            AS website,
    left(phones[1], 50)               AS phone
FROM read_parquet(
    's3://overturemaps-us-west-2/release/2026-09-23.1/theme=places/type=place/*',
    hive_partitioning = 1
    )
-- a box around sweden first, so duckdb can skip most of the world quickly
WHERE bbox.xmin BETWEEN 10.9 AND 24.2
  AND bbox.ymin BETWEEN 55.3 AND 69.1
-- the box also covers parts of norway, finland and denmark, so check the country too
  AND addresses[1].country = 'SE'
-- skip places overture is unsure about, they are often closed or duplicates
  AND confidence >= 0.5
  AND names.primary IS NOT NULL
-- food and drink only
  AND (
    taxonomy.primary LIKE '%restaurant'
   OR taxonomy.primary LIKE '%\_bar' ESCAPE '\'
   OR taxonomy.primary IN ('bar', 'pub', 'cafe', 'coffee_shop', 'bistro', 'brewery', 'brewpub', 'gastropub', 'winery')
    );

-- replace all old places with the new ones
DELETE FROM pg.public.place;

INSERT INTO pg.public.place (id, name, category, address, city, latitude, longitude, website, phone)
SELECT id, name, category, address, city, latitude, longitude, website, phone
FROM sweden_places;

-- show how many places were imported
SELECT count(*) AS imported_places FROM sweden_places;