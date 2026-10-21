package se.svent.tasting.restaurant;

import java.math.BigDecimal;

// one row per place in the everyone view, filled straight from the sql in RestaurantRepository
public interface PlaceSummary {
    String getPlaceId();
    String getName();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    Long getVisitedCount();
    Long getWantCount();
    BigDecimal getAverageScore();
}