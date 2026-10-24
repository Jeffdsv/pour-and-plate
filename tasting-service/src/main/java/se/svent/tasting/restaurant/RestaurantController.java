package se.svent.tasting.restaurant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// all urls here start with /api/restaurants
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService service;

    public RestaurantController(RestaurantService service) {
        this.service = service;
    }

    // GET /api/restaurants or /api/restaurants?status=VISITED, only my own
    @GetMapping
    public List<RestaurantResponse> findAll(@RequestParam(required = false) RestaurantStatus status,
                                            Authentication authentication) {
        return service.findAll(authentication.getName(), status);
    }

    // GET /api/restaurants/summary gives everyones places combined, for the everyone view
    @GetMapping("/summary")
    public List<PlaceSummary> summary() {
        return service.summarizeAllPlaces();
    }

    // GET /api/restaurants/together gives places two or more of us want to visit
    @GetMapping("/together")
    public List<GoTogetherPlace> together() {
        return service.findGoTogetherPlaces();
    }


    // GET /api/restaurants/top gives the best rated places, at least 2 reviews each, best first
    @GetMapping("/top")
    public List<TopPlace> top() {
        return service.findTopPlaces();
    }

    // GET /api/restaurants/taste gives my average ratings per kind of food, best first
    @GetMapping("/taste")
    public List<TasteRow> taste(Authentication authentication) {
        return service.findTasteProfile(authentication.getName());
    }

    // GET /api/restaurants/activity gives my friends newest reviews, at most 3 from each
    @GetMapping("/activity")
    public List<FriendActivity> activity(Authentication authentication) {
        return service.findFriendActivity(authentication.getName());
    }

    // GET /api/restaurants/5
    @GetMapping("/{id}")
    public RestaurantResponse findById(@PathVariable Long id, Authentication authentication) {
        return service.findById(authentication.getName(), id);
    }

    // POST /api/restaurants with json in the body, saved for the logged in user
    @PostMapping
    public RestaurantResponse save(@Valid @RequestBody SaveRestaurantRequest request,
                                   Authentication authentication) {
        return service.save(authentication.getName(), request);
    }

    // DELETE /api/restaurants/5, only my own, returns 204 with no body
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication authentication) {
        service.delete(authentication.getName(), id);
    }
}