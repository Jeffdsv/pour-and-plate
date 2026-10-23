# Place data scripts

These scripts fill the `place` table with food and drink places in Sweden, from two open datasets:
[Overture Maps](https://overturemaps.org) and [OpenStreetMap](https://www.openstreetmap.org).
My own reviews live in a separate table and are never changed by these scripts.

## Before you start

- Docker is running with the database: `docker compose up -d` in the project folder
- The backend has started at least once, so Flyway has created all tables and columns
- [DuckDB](https://duckdb.org) is installed

## Monthly refresh, in this order

| Step | What it does | How to run |
| --- | --- | --- |
| 1. `import_places.sql` | Imports Overture places for Sweden, skips places marked closed. Change the release date at the top to the newest on [stac.overturemaps.org](https://stac.overturemaps.org) | In `duckdb`: `.read scripts/import_places.sql` |
| 2. `clean_places.sql` | Removes clear duplicates: very similar names close together or at the same address. Never removes a reviewed place | `Get-Content scripts\clean_places.sql \| docker exec -i pourplate-postgres psql -U pourplate -d tasting` |
| 3. Download OpenStreetMap | Sweden from Geofabrik, about 900 MB | `curl.exe -L -o D:\osm\sweden-latest.osm.pbf https://download.geofabrik.de/europe/sweden-latest.osm.pbf` |
| 4. `import_osm.sql` | Reads restaurants, cafés, bars and pubs from the OSM file into `osm_place`, both points and building outlines | In `duckdb`: `.read scripts/import_osm.sql` |
| 5. `merge_osm.sql` | Links Overture places to matching OSM places by distance and name, and adds places only OSM has | `Get-Content scripts\merge_osm.sql \| docker exec -i pourplate-postgres psql -U pourplate -d tasting` |
| 6. `mark_replaced.sql` | Hides places that were probably replaced by another place at the same address. They are still found in search | `Get-Content scripts\mark_replaced.sql \| docker exec -i pourplate-postgres psql -U pourplate -d tasting` |

In DuckDB, type `.read` with no spaces before the dot, otherwise DuckDB waits for more SQL.

## Copy to Azure

1. Open the database firewall for your IP (find it on https://api.ipify.org):
   `az postgres flexible-server firewall-rule create --resource-group pour-and-plate --server-name pourplate-db-ne --name my-home --start-ip-address MY-IP --end-ip-address MY-IP`
2. Dump the place data only:
   `docker exec pourplate-postgres pg_dump -U pourplate -d tasting --data-only --format=custom -t place -t osm_place -f /tmp/places.dump`
3. Empty the tables in Azure, reviews are not touched:
   `docker exec -e PGPASSWORD='...' pourplate-postgres psql -h pourplate-db-ne.postgres.database.azure.com -U pourplate -d tasting -c "TRUNCATE place, osm_place;"`
4. Load the new data:
   `docker exec -e PGPASSWORD='...' pourplate-postgres pg_restore --data-only --no-owner -h pourplate-db-ne.postgres.database.azure.com -U pourplate -d tasting /tmp/places.dump`
5. Run step 6 (`mark_replaced.sql`) against Azure too, so it checks the reviews there:
   `Get-Content scripts\mark_replaced.sql | docker exec -i -e PGPASSWORD='...' pourplate-postgres psql -h pourplate-db-ne.postgres.database.azure.com -U pourplate -d tasting`
6. Close the firewall again:
   `az postgres flexible-server firewall-rule delete --resource-group pour-and-plate --server-name pourplate-db-ne --name my-home --yes`

## Checking before changing

- `preview_duplicates.sql` shows what step 2 would remove
- `preview_osm_match.sql` shows how OSM matches Overture before step 5
- `preview_replaced.sql` shows what step 6 would hide