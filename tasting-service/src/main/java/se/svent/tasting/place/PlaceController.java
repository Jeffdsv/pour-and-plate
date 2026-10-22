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
}
