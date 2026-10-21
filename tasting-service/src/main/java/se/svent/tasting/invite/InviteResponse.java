package se.svent.tasting.invite;

import java.time.Instant;

// what the admin gets back: the link to send, and when it stops working
public record InviteResponse(String link, Instant expiresAt) {
}
