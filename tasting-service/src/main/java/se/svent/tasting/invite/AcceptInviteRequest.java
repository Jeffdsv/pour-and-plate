package se.svent.tasting.invite;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// what a friend fills in on the sign up page
public record AcceptInviteRequest(
        // only small letters, numbers, dot and underscore, so usernames stay simple
        @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "[a-z0-9._]+") String username,
        @NotBlank @Size(max = 100) String displayName,
        // bcrypt only looks at the first 72 bytes, so longer passwords are refused
        @NotBlank @Size(min = 8, max = 72) String password
) {
}