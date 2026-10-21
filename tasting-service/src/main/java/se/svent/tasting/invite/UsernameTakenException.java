package se.svent.tasting.invite;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException(String username) {
        super("the username " + username + " is already taken");
    }
}