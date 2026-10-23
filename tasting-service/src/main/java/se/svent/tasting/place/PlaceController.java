package se.svent.tasting.place;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService service;

    public PlaceController(PlaceService service) {
        this.service = service;
    }

    // GET /api/places?south=59.32&west=18.05&north=59.34&east=18.09
    @GetMapping
    public List<PlaceResponse> findInArea(@RequestParam double south,
                                          @RequestParam double west,
                                          @RequestParam double north,
                                          @RequestParam double east,
                                          @RequestParam(required = false) List<PlaceKind> kinds) {
        return service.findInArea(south, west, north, east, kinds);
    }

    // GET /api/places/search?q=ekst gives places with ekst in the name, best match first
    @GetMapping("/search")
    public List<PlaceResponse> search(@RequestParam String q) {
        return service.search(q);
    }

    // GET /api/places/near?lat=59.33&lon=18.06&radius=1000&kinds=BEER gives the closest places first
    @GetMapping("/near")
    public List<NearbyPlace> near(@RequestParam double lat,
                                  @RequestParam double lon,
                                  @RequestParam(defaultValue = "1000") int radius,
                                  @RequestParam(required = false) List<PlaceKind> kinds) {
        return service.findNear(lat, lon, radius, kinds);
    }
}
