package se.svent.tasting.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

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
}