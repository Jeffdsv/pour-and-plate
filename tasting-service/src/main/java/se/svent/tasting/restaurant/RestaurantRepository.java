package se.svent.tasting.restaurant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring creates the code for this automatically when the app starts
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // All visited or all want-to-visit
    List<Restaurant> findByStatus(RestaurantStatus status);

    // Find by OpenStreetMap id. Empty if not saved yet.
    Optional<Restaurant> findByOsmId(String osmId);
}