package se.svent.tasting.restaurant;

import java.math.BigDecimal;
import java.time.Instant;

// one thing a friend did lately, a visit or a place they want to go to
public interface FriendActivity {
    Long getId();
    String getName();
    String getStatus();
    BigDecimal getScore();
    Instant getUpdatedAt();
    String getDisplayName();
}