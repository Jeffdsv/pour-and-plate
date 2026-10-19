package se.svent.tasting.restaurant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    // GET /api/restaurants or /api/restaurants?status=VISITED
    @GetMapping
    public List<RestaurantResponse> findAll(@RequestParam(required = false) RestaurantStatus status) {
        return service.findAll(status);
    }

    // GET /api/restaurants/5
    @GetMapping("/{id}")
    public RestaurantResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    // POST /api/restaurants with json in the body
    // @Valid makes spring run the checks in SaveRestaurantRequest
    @PostMapping
    public RestaurantResponse save(@Valid @RequestBody SaveRestaurantRequest request) {
        return service.save(request);
    }

    // DELETE /api/restaurants/5, returns 204 with no body
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}