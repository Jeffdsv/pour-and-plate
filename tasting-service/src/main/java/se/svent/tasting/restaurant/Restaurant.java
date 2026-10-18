package se.svent.tasting.restaurant;

import jakarta.persistence.*;
import se.svent.tasting.common.BaseEntity;
import se.svent.tasting.common.Ratings;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "restaurant")
public class Restaurant extends BaseEntity {

    @Column(name = "osm_id", unique = true, length = 50)
    private String osmId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    // Stored as text: "VISITED" / "WANT_TO_VISIT"
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RestaurantStatus status;

    @Column(name = "food_rating")
    private Integer foodRating;

    @Column(name = "atmosphere_rating")
    private Integer atmosphereRating;

    @Column(name = "value_rating")
    private Integer valueRating;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "visited_on")
    private LocalDate visitedOn;

    // Empty constructor JPA needs to load rows
    protected Restaurant() {
    }

    // Shared start for both options below. Private: use wantToVisit() or visited().
    private Restaurant(String name, BigDecimal latitude, BigDecimal longitude) {
        setName(name);
        setLocation(latitude, longitude);
    }

    // Option 1: save for later, no ratings
    public static Restaurant wantToVisit(String name, BigDecimal latitude, BigDecimal longitude) {
        Restaurant restaurant = new Restaurant(name, latitude, longitude);
        restaurant.status = RestaurantStatus.WANT_TO_VISIT;
        return restaurant;
    }

    // Option 2: already been there, all 3 ratings required
    public static Restaurant visited(String name, BigDecimal latitude, BigDecimal longitude,
                                     Integer food, Integer atmosphere, Integer value,
                                     LocalDate visitedOn) {
        Restaurant restaurant = new Restaurant(name, latitude, longitude);
        restaurant.review(food, atmosphere, value, visitedOn);
        return restaurant;
    }

    // Set or change the 3 ratings. Also marks it as visited.
    public void review(Integer food, Integer atmosphere, Integer value, LocalDate visitedOn) {
        this.foodRating = Ratings.requireValid(food, "foodRating");
        this.atmosphereRating = Ratings.requireValid(atmosphere, "atmosphereRating");
        this.valueRating = Ratings.requireValid(value, "valueRating");
        this.visitedOn = (visitedOn != null) ? visitedOn : LocalDate.now();
        this.status = RestaurantStatus.VISITED;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name.trim();
    }

    public void setLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("latitude and longitude are required");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Back to the wishlist: ratings are removed
    public void moveToWantToVisit() {
        this.foodRating = null;
        this.atmosphereRating = null;
        this.valueRating = null;
        this.visitedOn = null;
        this.status = RestaurantStatus.WANT_TO_VISIT;
    }

    // My score = average of the 3 ratings, 1 decimal. null if not visited.
    public Double getMyScore() {
        if (status != RestaurantStatus.VISITED) {
            return null;
        }
        double average = (foodRating + atmosphereRating + valueRating) / 3.0;
        return Math.round(average * 10) / 10.0;
    }

    public String getOsmId() { return osmId; }
    public void setOsmId(String osmId) { this.osmId = osmId; }

    public String getName() { return name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public BigDecimal getLatitude() { return latitude; }
    public BigDecimal getLongitude() { return longitude; }

    public RestaurantStatus getStatus() { return status; }

    public Integer getFoodRating() { return foodRating; }
    public Integer getAtmosphereRating() { return atmosphereRating; }
    public Integer getValueRating() { return valueRating; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getVisitedOn() { return visitedOn; }


}
