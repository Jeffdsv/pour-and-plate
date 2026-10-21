package se.svent.tasting.user;

// one row from the user summary query, spring fills it in by matching the names
public interface UserSummary {
    Long getId();
    String getUsername();
    String getDisplayName();
    String getRole();
    Long getVisitedCount();
    Long getWantCount();
}