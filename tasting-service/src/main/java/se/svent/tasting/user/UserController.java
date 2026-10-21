package se.svent.tasting.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.svent.tasting.restaurant.RestaurantResponse;
import se.svent.tasting.restaurant.RestaurantService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository users;
    private final RestaurantService restaurants;

    public UserController(AppUserRepository users, RestaurantService restaurants) {
        this.users = users;
        this.restaurants = restaurants;
    }

    // GET /api/users gives everyone with an account plus their visited and want to go counts
    @GetMapping
    public List<UserSummary> findAll() {
        return users.summarizeUsers();
    }

    // GET /api/users/3/restaurants gives someone elses map, read only
    @GetMapping("/{id}/restaurants")
    public List<RestaurantResponse> restaurantsOf(@PathVariable Long id) {
        return restaurants.findAllForUser(id);
    }
}