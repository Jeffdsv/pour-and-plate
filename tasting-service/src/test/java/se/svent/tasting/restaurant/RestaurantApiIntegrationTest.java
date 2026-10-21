package se.svent.tasting.restaurant;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import se.svent.tasting.TestcontainersConfiguration;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// starts the whole app on a random port with a real postgres in docker
// and sends real http requests to it, logged in like a real browser
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        // the test database has no admin password, so give it one only for tests
        properties = "app.admin.password=test-password")
@Import(TestcontainersConfiguration.class)
class RestaurantApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RestaurantRepository repository;

    // a cookie jar, keeps the session cookie and the csrf cookie between requests like a browser does
    private final CookieManager cookies = new CookieManager();
    private final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();

    // every test starts with an empty table and a logged in user
    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();
        login();
    }

    // full round trip: save over http, read it back, check the score
    @Test
    void saveVisited_thenFetchById_returnsScore() throws Exception {
        HttpResponse<String> saved = post("""
                {
                  "placeId": "node/1", "name": "Ekstedt",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "VISITED",
                  "foodRating": 9, "atmosphereRating": 8, "valueRating": 6
                }
                """);
        assertThat(saved.statusCode()).isEqualTo(200);

        Integer id = JsonPath.read(saved.body(), "$.id");

        HttpResponse<String> fetched = get("/api/restaurants/" + id);
        assertThat(fetched.statusCode()).isEqualTo(200);
        Double score = JsonPath.read(fetched.body(), "$.myScore");
        assertThat(score).isEqualTo(7.7);
    }

    // same place twice should still be one row in the real database
    @Test
    void saveSamePlaceIdTwice_keepsOneRow() throws Exception {
        String json = """
                {
                  "placeId": "node/1", "name": "Ekstedt",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "WANT_TO_VISIT"
                }
                """;
        post(json);
        post(json);

        assertThat(repository.count()).isEqualTo(1);
    }

    // bad data should give 400 with our message and save nothing
    @Test
    void visitedWithoutRatings_returns400_andSavesNothing() throws Exception {
        HttpResponse<String> response = post("""
                {
                  "name": "Bad Test",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "VISITED"
                }
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("foodRating is required");
        assertThat(repository.count()).isZero();
    }

    // an id that doesnt exist should give 404
    @Test
    void unknownId_returns404() throws Exception {
        HttpResponse<String> response = get("/api/restaurants/999");

        assertThat(response.statusCode()).isEqualTo(404);
    }

    // without logging in, the api should refuse with 401
    @Test
    void notLoggedIn_returns401() throws Exception {
        // a fresh client with no cookies, so no session
        HttpClient anonymous = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/restaurants"))).GET().build();

        HttpResponse<String> response = anonymous.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(401);
    }

    // logged in but without the csrf header, saving should be blocked with 403
    @Test
    void postWithoutCsrfToken_returns403() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/restaurants")))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        { "name": "Test", "latitude": 59.33, "longitude": 18.06, "status": "WANT_TO_VISIT" }
                        """))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(repository.count()).isZero();
    }

    // helpers

    // logs in like the frontend does: get a csrf cookie first, then send username and password
    private void login() throws Exception {
        get("/api/auth/me");

        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/auth/login")))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString("""
                        { "username": "svent", "password": "test-password" }
                        """))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
    }

    // finds the csrf token spring put in the cookie jar
    private String csrfToken() {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse("");
    }

    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/restaurants")))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path))).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}