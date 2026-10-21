package se.svent.tasting.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, String> {

    // all places inside the visible map area, uses the index on latitude and longitude
    @Query(value = """
            SELECT *
            FROM place
            WHERE latitude BETWEEN :south AND :north
              AND longitude BETWEEN :west AND :east
            LIMIT :maxResults
            """, nativeQuery = true)
    List<Place> findInArea(@Param("south") double south,
                           @Param("west") double west,
                           @Param("north") double north,
                           @Param("east") double east,
                           @Param("maxResults") int maxResults);
}
