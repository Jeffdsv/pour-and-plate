package se.svent.tasting.place;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlaceService {

    // never send more than this many places in one answer
    private static final int MAX_RESULTS = 3000;

    private static final int MAX_SEARCH_RESULTS = 20;

    // about how many meters one degree of latitude is, used to turn the radius into a box
    private static final double METERS_PER_DEGREE = 111_320;

    // near me never looks further than this
    private static final int MAX_RADIUS_METERS = 5000;

    // biggest allowed area in degrees, roughly 20 km, so nobody asks for all of sweden at once
    private static final double MAX_SPAN = 0.2;

    private final PlaceRepository repository;

    public PlaceService(PlaceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> findInArea(double south, double west, double north, double east, List<PlaceKind> kinds) {
        // the edges must make sense, otherwise its a bad request
        if (south >= north || west >= east) {
            throw new IllegalArgumentException("south must be below north and west must be left of east");
        }
        if (north - south > MAX_SPAN || east - west > MAX_SPAN) {
            throw new IllegalArgumentException("area is too big, zoom in");
        }

        // no kinds chosen means all of them, so old calls without the parameter still work
        List<PlaceKind> chosen = (kinds == null || kinds.isEmpty()) ? List.of(PlaceKind.values()) : kinds;
        List<String> kindNames = chosen.stream().map(Enum::name).toList();

        // the middle of the visible area, places closest to it come first
        double centerLat = (south + north) / 2;
        double centerLon = (west + east) / 2;

        return repository.findInArea(south, west, north, east, centerLat, centerLon, kindNames, MAX_RESULTS)
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceResponse> search(String text) {
        String trimmed = (text == null) ? "" : text.trim();

        // one letter matches almost everything, so wait until there are at least two
        if (trimmed.length() < 2) {
            return List.of();
        }

        // in ILIKE, % means any text and _ means any letter, so escape them to search for them as normal letters
        String pattern = trimmed
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        return repository.searchByName(pattern, trimmed, MAX_SEARCH_RESULTS)
                .stream()
                .map(PlaceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NearbyPlace> findNear(double lat, double lon, int radius, List<PlaceKind> kinds) {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("lat must be between -90 and 90 and lon between -180 and 180");
        }
        if (radius < 1 || radius > MAX_RADIUS_METERS) {
            throw new IllegalArgumentException("radius must be between 1 and " + MAX_RADIUS_METERS + " meters");
        }

        // no kinds chosen means all of them
        List<PlaceKind> chosen = (kinds == null || kinds.isEmpty()) ? List.of(PlaceKind.values()) : kinds;
        List<String> kindNames = chosen.stream().map(Enum::name).toList();

        // a box around the point that the circle fits inside
        // degrees of longitude get shorter further north, so the box is wider there
        double latDelta = radius / METERS_PER_DEGREE;
        double lonDelta = radius / (METERS_PER_DEGREE * Math.cos(Math.toRadians(lat)));

        return repository.findNear(lat, lon,
                lat - latDelta, lat + latDelta,
                lon - lonDelta, lon + lonDelta,
                radius, kindNames, MAX_RESULTS);
    }
}
