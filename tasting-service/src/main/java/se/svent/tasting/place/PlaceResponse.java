package se.svent.tasting.place;

import java.math.BigDecimal;

// what the api sends to the map for each place
public record PlaceResponse(
        String id,
        String name,
        String category,
        String address,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        String website,
        String kind,
        boolean likelyClosed
) {

    // turns a Place from the database into a response
    public static PlaceResponse from(Place p) {
        return new PlaceResponse(
                p.getId(), p.getName(), p.getCategory(), p.getAddress(), p.getCity(),
                p.getLatitude(), p.getLongitude(), p.getWebsite(), p.getKind(), p.isLikelyClosed()
        );
    }
}
