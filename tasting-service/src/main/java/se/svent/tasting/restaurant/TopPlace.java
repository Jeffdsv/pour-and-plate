package se.svent.tasting.restaurant;

import java.math.BigDecimal;

// one row in the top rated list, filled straight from the sql in RestaurantRepository
public interface TopPlace {
    String getPlaceId();
    String getName();
    BigDecimal getLatitude();
    BigDecimal getLongitude();

    // how many visited reviews the place has, always 2 or more
    Long getReviewCount();
    BigDecimal getAverageScore();

    // 1 is the best, places with the same score and count share a rank
    Long getRank();
}