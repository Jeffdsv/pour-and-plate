package se.svent.tasting.restaurant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;

    // Spring gives the repository automatically (dependency injection)
    public RestaurantService(RestaurantRepository repository) {
        this.repository = repository;
    }

    // All restaurants, or only one status if given
    @Transactional(readOnly = true)
    public List<RestaurantResponse> findAll(RestaurantStatus status) {
        List<Restaurant> restaurants = (status == null)
                ? repository.findAll()
                : repository.findByStatus(status);

        return restaurants.stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse findById(Long id) {
        return RestaurantResponse.from(getOrThrow(id));
    }

    // Save a restaurant. Same OpenStreetMap place again = update, not a duplicate.
    @Transactional
    public RestaurantResponse save(SaveRestaurantRequest request) {
        Restaurant restaurant = (request.placeId() == null)
                ? null
                : repository.findByPlaceId(request.placeId()).orElse(null);

        if (restaurant == null) {
            restaurant = create(request);
        } else {
            restaurant.setName(request.name());
            restaurant.setLocation(request.latitude(), request.longitude());
            applyStatus(restaurant, request);
        }

        restaurant.setPlaceId(request.placeId());
        restaurant.setAddress(request.address());
        restaurant.setCity(request.city());
        restaurant.setNotes(request.notes());

        return RestaurantResponse.from(repository.save(restaurant));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    // Find the restaurant with this id, and if it doesnt exist, throw the not found error

    private Restaurant getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    // Uses the two "factory" methods from Restaurant
    private Restaurant create(SaveRestaurantRequest r) {
        if (r.status() == RestaurantStatus.VISITED) {
            return Restaurant.visited(r.name(), r.latitude(), r.longitude(),
                    r.foodRating(), r.atmosphereRating(), r.valueRating(), r.visitedOn());
        }
        return Restaurant.wantToVisit(r.name(), r.latitude(), r.longitude());
    }

    // Change status on an existing restaurant
    private void applyStatus(Restaurant restaurant, SaveRestaurantRequest r) {
        if (r.status() == RestaurantStatus.VISITED) {
            restaurant.review(r.foodRating(), r.atmosphereRating(), r.valueRating(), r.visitedOn());
        } else {
            restaurant.moveToWantToVisit();
        }
    }
}