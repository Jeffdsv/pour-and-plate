package se.svent.tasting.common;

// Checks that a rating is a whole number from 1 to 10.
// Used by every class that has a rating

public final class Ratings {

    public static final int MIN = 1;
    public static final int MAX = 10;

    private Ratings() {
    }

    // Returns the rating if valid, otherwise throws an error.
    public static Integer requireValid(Integer rating, String fieldName) {
        if (rating == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (rating < MIN || rating > MAX) {
            throw new IllegalArgumentException(
                    fieldName + " must be between " + MIN + " and " + MAX + ", was " + rating);
        }
        return rating;
    }
}
