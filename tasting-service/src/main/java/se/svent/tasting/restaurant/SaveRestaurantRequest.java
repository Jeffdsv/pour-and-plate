package se.svent.tasting.restaurant;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

// What the app sends when you click Visited or Want to visit
public record SaveRestaurantRequest(

        String placeId,

        // @NotBlank Rejects null (not sent), "" (empty), and " "
        @NotBlank @Size(max = 200)
        String name,

        @Size(max = 255)
        String address,

        @Size(max = 100)
        String city,

        @NotNull @DecimalMin("-90") @DecimalMax("90")
        BigDecimal latitude,

        @NotNull @DecimalMin("-180") @DecimalMax("180")
        BigDecimal longitude,

        @NotNull
        RestaurantStatus status,

        // Only for VISITED. Empty for WANT_TO_VISIT.
        @Min(1) @Max(10) Integer foodRating,
        @Min(1) @Max(10) Integer atmosphereRating,
        @Min(1) @Max(10) Integer valueRating,

        String notes,

        LocalDate visitedOn,

        @Size(max = 1000)
        String website
) {

}