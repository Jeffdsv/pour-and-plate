package se.svent.tasting.restaurant;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// tests for Restaurant no spring and no database so they run super fast
class RestaurantTest {

    // same test coordinates for every test
    private static final BigDecimal LAT = new BigDecimal("59.33");
    private static final BigDecimal LON = new BigDecimal("18.06");

    // new wishlist place should have no ratings and no score
    @Test
    void wantToVisit_hasNoRatingsAndNoScore() {

        Restaurant restaurant = Restaurant.wantToVisit("Ekstedt", LAT, LON);

        // status is right and everything rating related is empty
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.WANT_TO_VISIT);
        assertThat(restaurant.getFoodRating()).isNull();
        assertThat(restaurant.getMyScore()).isNull();
    }

    // checks the average and rounds up
    @Test
    void visited_calculatesScoreRoundedToOneDecimal() {
        Restaurant restaurant = Restaurant.visited("Ekstedt", LAT, LON, 9, 8, 6, null);

        // (9 + 8 + 6) / 3 = 7.666 which rounds to 7.7
        assertThat(restaurant.getMyScore()).isEqualTo(7.7);
        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.VISITED);
    }

    // no date sent means review() should use today
    @Test
    void visited_withoutDate_usesToday() {
        Restaurant restaurant = Restaurant.visited("Ekstedt", LAT, LON, 9, 8, 6, null);

        assertThat(restaurant.getVisitedOn()).isEqualTo(LocalDate.now());
    }

    // ratings must be 1 to 10
    @Test
    void review_withRatingAboveTen_throws() {
        Restaurant restaurant = Restaurant.wantToVisit("Ekstedt", LAT, LON);

        // the test passes only if this line throws the error
        assertThatThrownBy(() -> restaurant.review(11, 8, 6, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("foodRating");
    }

    // a visited place needs all 3 ratings
    @Test
    void review_withMissingRating_throws() {
        Restaurant restaurant = Restaurant.wantToVisit("Ekstedt", LAT, LON);

        assertThatThrownBy(() -> restaurant.review(9, null, 6, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("atmosphereRating");
    }

    // moving back to the wishlist should wipe all ratings and the date
    // if it didnt, the database rule ratings_match_status would refuse to save
    @Test
    void moveToWantToVisit_clearsAllRatings() {

        Restaurant restaurant = Restaurant.visited("Ekstedt", LAT, LON, 9, 8, 6, null);

        restaurant.moveToWantToVisit();

        assertThat(restaurant.getStatus()).isEqualTo(RestaurantStatus.WANT_TO_VISIT);
        assertThat(restaurant.getFoodRating()).isNull();
        assertThat(restaurant.getAtmosphereRating()).isNull();
        assertThat(restaurant.getValueRating()).isNull();
        assertThat(restaurant.getVisitedOn()).isNull();
    }

    @Test
    void blankName_throws() {
        assertThatThrownBy(() -> Restaurant.wantToVisit("   ", LAT, LON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void name_isTrimmed() {
        Restaurant restaurant = Restaurant.wantToVisit("  Ekstedt  ", LAT, LON);

        assertThat(restaurant.getName()).isEqualTo("Ekstedt");
    }
}
