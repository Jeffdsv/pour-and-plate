package se.svent.tasting.place;

import java.math.BigDecimal;

// one place close to me, the same fields as a place plus how far away it is
public interface NearbyPlace {
    String getId();
    String getName();
    String getCategory();
    String getAddress();
    String getCity();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getWebsite();
    String getKind();
    Double getDistanceMeters();
}