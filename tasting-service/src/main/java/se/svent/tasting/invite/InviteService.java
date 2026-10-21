package se.svent.tasting.invite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.svent.tasting.user.AppUser;
import se.svent.tasting.user.AppUserRepository;
import se.svent.tasting.user.Role;
import se.svent.tasting.user.UserResponse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class InviteService {

    // how long a link works
    private static final Duration LIFETIME = Duration.ofMinutes(15);

    // a random generator made for secrets, normal Random is guessable
    private final SecureRandom random = new SecureRandom();

    private final InviteRepository invites;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String frontendUrl;

    public InviteService(InviteRepository invites,
                         AppUserRepository users,
                         PasswordEncoder passwordEncoder,
                         Clock clock,
                         @Value("${app.frontend-url}") String frontendUrl) {
        this.invites = invites;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.frontendUrl = frontendUrl;
    }

    // makes a new link, only the hash is saved, the real token only exists in the link
    @Transactional
    public InviteResponse create(String adminUsername) {
        AppUser admin = users.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalStateException("logged in user not found"));

        String token = newToken();
        Instant expiresAt = clock.instant().plus(LIFETIME);
        invites.save(new Invite(hash(token), admin, expiresAt));

        return new InviteResponse(frontendUrl + "/invite/" + token, expiresAt);
    }

    // true if the link can still be used
    @Transactional(readOnly = true)
    public boolean isValid(String token) {
        return invites.findByTokenHash(hash(token))
                .map(invite -> invite.isUsableAt(clock.instant()))
                .orElse(false);
    }

    // creates the friend account and uses up the link, all or nothing
    @Transactional
    public UserResponse accept(String token, AcceptInviteRequest request) {
        if (users.existsByUsername(request.username())) {
            throw new se.svent.tasting.invite.UsernameTakenException(request.username());
        }

        AppUser friend = users.save(new AppUser(
                request.username(),
                request.displayName(),
                passwordEncoder.encode(request.password()),
                Role.FRIEND));

        // if the link was not usable, throwing here rolls back the new account too
        int claimed = invites.claim(hash(token), friend.getId(), clock.instant());
        if (claimed == 0) {
            throw new InvalidInviteException();
        }

        return UserResponse.from(friend);
    }

    // 32 random bytes written as text that is safe to put in a url
    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // sha 256 of the token as 64 hex characters, the same token always gives the same hash
    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("sha 256 is missing from java", e);
        }
    }
}