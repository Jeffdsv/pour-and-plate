package se.svent.tasting.restaurant;

import java.math.BigDecimal;

// one place that two or more of us want to visit, filled in by the go together query
public interface GoTogetherPlace {
    String getPlaceId();
    String getName();
    String getAddress();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    Long getWantCount();
    String getWantBy();
}