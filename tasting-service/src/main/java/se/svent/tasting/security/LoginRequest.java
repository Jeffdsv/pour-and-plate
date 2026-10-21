package se.svent.tasting.security;

import jakarta.validation.constraints.NotBlank;

// what the login page sends
public record LoginRequest(@NotBlank String username, @NotBlank String password) {
}
