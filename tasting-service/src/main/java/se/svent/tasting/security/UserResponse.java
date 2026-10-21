package se.svent.tasting.user;

// what the frontend gets to know about a user, never the password hash
public record UserResponse(Long id, String username, String displayName, Role role) {

    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getDisplayName(), u.getRole());
    }
}
