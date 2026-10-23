package se.svent.tasting.restaurant;

import java.math.BigDecimal;

// one row in my taste profile: a kind of food and how i usually rate it
public interface TasteRow {
    String getCategory();
    Long getVisits();
    BigDecimal getAverageScore();
    BigDecimal getAverageFood();
    BigDecimal getAverageAtmosphere();
    BigDecimal getAverageValue();
}