package se.svent.tasting.restaurant;

import java.math.BigDecimal;
import java.time.LocalDate;

// What the API sends back
public record RestaurantResponse(
        Long id,
        String placeId,
        String name,
        String address,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        RestaurantStatus status,
        Integer foodRating,
        Integer atmosphereRating,
        Integer valueRating,
        Double myScore,
        String notes,
        LocalDate visitedOn
) {

    // Converts a Restaurant (database) into a response (API)
    public static RestaurantResponse from(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getPlaceId(),
                r.getName(),
                r.getAddress(),
                r.getCity(),
                r.getLatitude(),
                r.getLongitude(),
                r.getStatus(),
                r.getFoodRating(),
                r.getAtmosphereRating(),
                r.getValueRating(),
                r.getMyScore(),
                r.getNotes(),
                r.getVisitedOn()
        );
    }
}