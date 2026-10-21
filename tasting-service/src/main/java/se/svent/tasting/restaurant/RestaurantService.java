package se.svent.tasting.restaurant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.svent.tasting.user.AppUser;
import se.svent.tasting.user.AppUserRepository;

import java.util.List;

@Service
public class RestaurantService {

    private final RestaurantRepository repository;
    private final AppUserRepository users;

    public RestaurantService(RestaurantRepository repository, AppUserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    // all my reviews, or only one status if given
    @Transactional(readOnly = true)
    public List<RestaurantResponse> findAll(String username, RestaurantStatus status) {

        AppUser me = currentUser(username);
        List<Restaurant> restaurants = (status == null)
                ? repository.findByUserId(me.getId())
                : repository.findByUserIdAndStatus(me.getId(), status);

        return restaurants.stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    // anyone logged in may look at anyones map, but only read it, changing still checks the owner
    @Transactional(readOnly = true)
    public List<RestaurantResponse> findAllForUser(Long userId) {

        return repository.findByUserId(userId).stream()
                .map(RestaurantResponse::from)
                .toList();
    }

    // every place anyone has saved, with counts and the shared average score
    @Transactional(readOnly = true)
    public List<PlaceSummary> summarizeAllPlaces() {
        return repository.summarizeAllPlaces();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse findById(String username, Long id) {
        return RestaurantResponse.from(getOwnOrThrow(username, id));
    }

    // saves a review for me, my same place again means update, not a duplicate
    @Transactional
    public RestaurantResponse save(String username, SaveRestaurantRequest request) {
        AppUser me = currentUser(username);

        Restaurant restaurant = (request.placeId() == null)
                ? null
                : repository.findByUserIdAndPlaceId(me.getId(), request.placeId()).orElse(null);

        if (restaurant == null) {
            restaurant = create(me, request);
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
    public void delete(String username, Long id) {
        repository.delete(getOwnOrThrow(username, id));
    }

    // helpers, only used inside this class

    // the logged in user, always exists because spring security already checked the login
    private AppUser currentUser(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("logged in user not found"));
    }

    // finds a review by id, but only if it is mine, otherwise 403
    private Restaurant getOwnOrThrow(String username, Long id) {
        Restaurant restaurant = repository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));

        if (!restaurant.isOwnedBy(currentUser(username))) {
            throw new AccessDeniedException("you can only change your own reviews");
        }
        return restaurant;
    }

    private Restaurant create(AppUser owner, SaveRestaurantRequest r) {
        if (r.status() == RestaurantStatus.VISITED) {
            return Restaurant.visited(owner, r.name(), r.latitude(), r.longitude(),
                    r.foodRating(), r.atmosphereRating(), r.valueRating(), r.visitedOn());
        }
        return Restaurant.wantToVisit(owner, r.name(), r.latitude(), r.longitude());
    }

    private void applyStatus(Restaurant restaurant, SaveRestaurantRequest r) {
        if (r.status() == RestaurantStatus.VISITED) {
            restaurant.review(r.foodRating(), r.atmosphereRating(), r.valueRating(), r.visitedOn());
        } else {
            restaurant.moveToWantToVisit();
        }
    }
}