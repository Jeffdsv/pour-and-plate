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
    // one row per place: how many visited it, who they are, how many want to go, and the average score
    @Query(value = """
            SELECT r.place_id                                                  AS "placeId",
                   MIN(r.name)                                                 AS "name",
                   MIN(r.latitude)                                             AS "latitude",
                   MIN(r.longitude)                                            AS "longitude",
                   COUNT(*) FILTER (WHERE r.status = 'VISITED')                AS "visitedCount",
                   COUNT(*) FILTER (WHERE r.status = 'WANT_TO_VISIT')          AS "wantCount",
                   ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) AS "averageScore",
                   STRING_AGG(u.display_name, ', ' ORDER BY u.display_name)
                       FILTER (WHERE r.status = 'VISITED')                     AS "visitedBy",
                   STRING_AGG(u.display_name, ', ' ORDER BY u.display_name)
                       FILTER (WHERE r.status = 'WANT_TO_VISIT')               AS "wantBy"
            FROM restaurant r
            JOIN app_user u ON u.id = r.user_id
            WHERE r.place_id IS NOT NULL
            GROUP BY r.place_id
            """, nativeQuery = true)
    List<PlaceSummary> summarizeAllPlaces();
}