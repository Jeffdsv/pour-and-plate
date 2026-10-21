package se.svent.tasting.invite;

// the link was already used, has expired, or never existed
public class InvalidInviteException extends RuntimeException {

    public InvalidInviteException() {
        super("this invite link is used or expired");
    }
}