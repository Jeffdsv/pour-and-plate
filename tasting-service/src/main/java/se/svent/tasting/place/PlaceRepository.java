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
}
