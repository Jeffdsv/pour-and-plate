package se.svent.tasting.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// Spring creates the code for this automatically when the app starts
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    // all my reviews
    List<Restaurant> findByUserId(Long userId);

    // my visited, or my want to visit
    List<Restaurant> findByUserIdAndStatus(Long userId, RestaurantStatus status);

    // my review of one place, empty if i have not saved it
    Optional<Restaurant> findByUserIdAndPlaceId(Long userId, String placeId);

    // one row per place with how many visited it, how many want to go, and the average score of everyone
    @Query(value = """
            SELECT place_id                                          AS "placeId",
                   MIN(name)                                         AS "name",
                   MIN(latitude)                                     AS "latitude",
                   MIN(longitude)                                    AS "longitude",
                   COUNT(*) FILTER (WHERE status = 'VISITED')        AS "visitedCount",
                   COUNT(*) FILTER (WHERE status = 'WANT_TO_VISIT')  AS "wantCount",
                   ROUND(AVG((food_rating + atmosphere_rating + value_rating) / 3.0), 1) AS "averageScore"
            FROM restaurant
            WHERE place_id IS NOT NULL
            GROUP BY place_id
            """, nativeQuery = true)
    List<PlaceSummary> summarizeAllPlaces();
}