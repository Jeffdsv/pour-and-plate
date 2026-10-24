package se.svent.tasting.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // places that two or more of us want to visit, the most wanted first
    // WHERE picks the rows before grouping, HAVING picks the groups after counting
    @Query(value = """
            SELECT r.place_id      AS "placeId",
                   MIN(r.name)      AS "name",
                   MIN(r.address)   AS "address",
                   MIN(r.latitude)  AS "latitude",
                   MIN(r.longitude) AS "longitude",
                   COUNT(*)         AS "wantCount",
                   STRING_AGG(u.display_name, ', ' ORDER BY u.display_name) AS "wantBy"
            FROM restaurant r
            JOIN app_user u ON u.id = r.user_id
            WHERE r.status = 'WANT_TO_VISIT'
              AND r.place_id IS NOT NULL
            GROUP BY r.place_id
            HAVING COUNT(*) >= 2
            ORDER BY COUNT(*) DESC, MIN(r.name)
            """, nativeQuery = true)
    List<GoTogetherPlace> findGoTogetherPlaces();

    // my taste profile: for each kind of food i have visited, how many and my average ratings
    // the category lives in place, so we join my reviews with the places they belong to
    @Query(value = """
            SELECT p.category AS "category",
                   COUNT(*)   AS "visits",
                   ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) AS "averageScore",
                   ROUND(AVG(r.food_rating), 1)       AS "averageFood",
                   ROUND(AVG(r.atmosphere_rating), 1) AS "averageAtmosphere",
                   ROUND(AVG(r.value_rating), 1)      AS "averageValue"
            FROM restaurant r
            JOIN place p ON p.id = r.place_id
            WHERE r.user_id = :userId
              AND r.status = 'VISITED'
            GROUP BY p.category
            ORDER BY "averageScore" DESC, "visits" DESC
            """, nativeQuery = true)
    List<TasteRow> findTasteProfile(@Param("userId") Long userId);

    // the newest reviews from my friends, at most perFriend from each, newest first
    // ROW_NUMBER numbers each friends reviews 1, 2, 3 from the newest, then we keep the first few
    @Query(value = """
            SELECT *
            FROM (
                SELECT r.id           AS "id",
                       r.name         AS "name",
                       r.status       AS "status",
                       ROUND((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0, 1) AS "score",
                       r.updated_at   AS "updatedAt",
                       u.display_name AS "displayName",
                       ROW_NUMBER() OVER (PARTITION BY r.user_id ORDER BY r.updated_at DESC) AS "rowNumber"
                FROM restaurant r
                JOIN app_user u ON u.id = r.user_id
                WHERE r.user_id <> :userId
            ) latest
            WHERE "rowNumber" <= :perFriend
            ORDER BY "updatedAt" DESC
            LIMIT 30
            """, nativeQuery = true)
    List<FriendActivity> findFriendActivity(@Param("userId") Long userId, @Param("perFriend") int perFriend);


    // the best rated places, a place needs at least 2 visited reviews to be ranked
    // RANK numbers the rows after grouping, places with the exact same score and count share a rank
    @Query(value = """
            SELECT r.place_id       AS "placeId",
                   MIN(r.name)      AS "name",
                   MIN(r.latitude)  AS "latitude",
                   MIN(r.longitude) AS "longitude",
                   COUNT(*)         AS "reviewCount",
                   ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) AS "averageScore",
                   RANK() OVER (
                       ORDER BY ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) DESC,
                                COUNT(*) DESC
                   ) AS "rank"
            FROM restaurant r
            WHERE r.status = 'VISITED'
              AND r.place_id IS NOT NULL
            GROUP BY r.place_id
            HAVING COUNT(*) >= 2
            ORDER BY "rank", MIN(r.name)
            LIMIT 20
            """, nativeQuery = true)
    List<TopPlace> findTopPlaces();


    // the same ranking, but only places of the chosen kinds, so the best bar becomes number 1
    // RANK runs after WHERE, so the numbers start at 1 within the chosen kinds
    @Query(value = """
            SELECT r.place_id       AS "placeId",
                   MIN(r.name)      AS "name",
                   MIN(r.latitude)  AS "latitude",
                   MIN(r.longitude) AS "longitude",
                   COUNT(*)         AS "reviewCount",
                   ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) AS "averageScore",
                   RANK() OVER (
                       ORDER BY ROUND(AVG((r.food_rating + r.atmosphere_rating + r.value_rating) / 3.0), 1) DESC,
                                COUNT(*) DESC
                   ) AS "rank"
            FROM restaurant r
            JOIN place p ON p.id = r.place_id
            WHERE r.status = 'VISITED'
              AND p.kind IN (:kinds)
            GROUP BY r.place_id
            HAVING COUNT(*) >= 2
            ORDER BY "rank", MIN(r.name)
            LIMIT 20
            """, nativeQuery = true)
    List<TopPlace> findTopPlacesOfKinds(@Param("kinds") List<String> kinds);
}