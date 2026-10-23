package se.svent.tasting.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, String> {

    // all places inside the visible map area, uses the index on latitude and longitude
    // places inside the visible map area and of the chosen kinds, closest to the middle of the screen first
    // so if we hit the limit its the places at the edges that get left out
    @Query(value = """
            SELECT *
            FROM place
            WHERE latitude BETWEEN :south AND :north
              AND longitude BETWEEN :west AND :east
              AND kind IN (:kinds)
            ORDER BY POWER(latitude - :centerLat, 2)
                   + POWER((longitude - :centerLon) * COS(RADIANS(:centerLat)), 2)
            LIMIT :maxResults
            """, nativeQuery = true)
    List<Place> findInArea(@Param("south") double south,
                           @Param("west") double west,
                           @Param("north") double north,
                           @Param("east") double east,
                           @Param("centerLat") double centerLat,
                           @Param("centerLon") double centerLon,
                           @Param("kinds") List<String> kinds,
                           @Param("maxResults") int maxResults);


    // places whose name contains the search text, the most similar names first
    // the pg_trgm index from V12 keeps the ILIKE fast even with % on both sides
    @Query(value = """
            SELECT *
            FROM place
            WHERE name ILIKE '%' || :pattern || '%'
            ORDER BY similarity(name, :text) DESC, name
            LIMIT :maxResults
            """, nativeQuery = true)
    List<Place> searchByName(@Param("pattern") String pattern,
                             @Param("text") String text,
                             @Param("maxResults") int maxResults);

    // places of the chosen kinds within radius meters of a point, closest first
    // the box (BETWEEN) uses the index to skip most of sweden, then haversine gives the exact distance
    @Query(value = """
            SELECT *
            FROM (
                SELECT p.id        AS "id",
                       p.name      AS "name",
                       p.category  AS "category",
                       p.address   AS "address",
                       p.city      AS "city",
                       p.latitude  AS "latitude",
                       p.longitude AS "longitude",
                       p.website   AS "website",
                       p.kind      AS "kind",
                       2 * 6371000 * ASIN(SQRT(
                           POWER(SIN(RADIANS(p.latitude - :lat) / 2), 2)
                         + COS(RADIANS(:lat)) * COS(RADIANS(p.latitude))
                         * POWER(SIN(RADIANS(p.longitude - :lon) / 2), 2)
                       )) AS "distanceMeters"
                FROM place p
                WHERE p.latitude BETWEEN :minLat AND :maxLat
                  AND p.longitude BETWEEN :minLon AND :maxLon
                  AND p.kind IN (:kinds)
            ) nearby
            WHERE "distanceMeters" <= :radius
            ORDER BY "distanceMeters"
            LIMIT :maxResults
            """, nativeQuery = true)
    List<NearbyPlace> findNear(@Param("lat") double lat,
                               @Param("lon") double lon,
                               @Param("minLat") double minLat,
                               @Param("maxLat") double maxLat,
                               @Param("minLon") double minLon,
                               @Param("maxLon") double maxLon,
                               @Param("radius") double radius,
                               @Param("kinds") List<String> kinds,
                               @Param("maxResults") int maxResults);
}
