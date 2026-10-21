package se.svent.tasting.invite;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import se.svent.tasting.ApiClient;
import se.svent.tasting.TestcontainersConfiguration;
import se.svent.tasting.user.AppUserRepository;

import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

// the whole invite flow over real http, with a real postgres in docker
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.admin.password=test-password")
@Import({TestcontainersConfiguration.class, InviteApiIntegrationTest.ClockConfig.class})
class InviteApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AppUserRepository users;

    @Autowired
    private MovableClock clock;

    // a link works once: the first friend gets an account, the next one gets 410
    @Test
    void inviteLink_worksOnlyOnce() throws Exception {
        String token = createInviteAsAdmin();

        ApiClient anna = new ApiClient(port);
        assertThat(anna.get("/api/invites/" + token).body()).contains("\"valid\":true");
        assertThat(accept(anna, token, "anna").statusCode()).isEqualTo(200);

        // the new account can log in
        assertThat(anna.login("anna", "anna-password").statusCode()).isEqualTo(200);

        // the same link again is dead
        assertThat(accept(new ApiClient(port), token, "anna2").statusCode()).isEqualTo(410);
    }

    // after 15 minutes the link is dead and no account is created
    @Test
    void inviteLink_expiresAfter15Minutes() throws Exception {
        String token = createInviteAsAdmin();

        // jump 16 minutes into the future instead of waiting
        clock.advance(Duration.ofMinutes(16));

        assertThat(accept(new ApiClient(port), token, "erik").statusCode()).isEqualTo(410);
        // the account was rolled back together with the failed claim
        assertThat(users.existsByUsername("erik")).isFalse();
    }

    // a friend is not the admin, so they cannot make new links
    @Test
    void friend_cannotCreateInvites() throws Exception {
        ApiClient bob = new ApiClient(port);
        accept(bob, createInviteAsAdmin(), "bob");
        bob.login("bob", "bob-password");

        assertThat(bob.post("/api/invites", "").statusCode()).isEqualTo(403);
    }

    // helpers

    // logs in as the admin, makes a link and returns only the token part at the end
    private String createInviteAsAdmin() throws Exception {
        ApiClient admin = new ApiClient(port);
        admin.login("svent", "test-password");

        HttpResponse<String> response = admin.post("/api/invites", "");
        assertThat(response.statusCode()).isEqualTo(200);

        String link = JsonPath.read(response.body(), "$.link");
        return link.substring(link.lastIndexOf('/') + 1);
    }

    // signs up with the link, the password is the username plus "-password"
    private HttpResponse<String> accept(ApiClient client, String token, String username) throws Exception {
        return client.post("/api/invites/" + token + "/accept", """
                { "username": "%s", "displayName": "%s", "password": "%s-password" }
                """.formatted(username, username, username));
    }

    // a clock the test can move forward, so expiry can be tested without waiting 15 minutes
    static class MovableClock extends Clock {

        private Instant now = Instant.now();

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }

    // gives the app the movable clock instead of the real one, only in this test
    @TestConfiguration
    static class ClockConfig {

        @Bean
        @Primary
        MovableClock movableClock() {
            return new MovableClock();
        }
    }
}