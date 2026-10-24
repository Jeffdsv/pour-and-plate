package se.svent.tasting.restaurant;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import se.svent.tasting.TestcontainersConfiguration;
import se.svent.tasting.user.AppUser;
import se.svent.tasting.user.AppUserRepository;
import se.svent.tasting.user.Role;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

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

    @Autowired
    private AppUserRepository users;

    // the taste profile joins with place, so the test writes a few places with plain sql
    @Autowired
    private JdbcTemplate jdbc;

    private static final BigDecimal LAT = new BigDecimal("59.33");
    private static final BigDecimal LON = new BigDecimal("18.06");

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

    // i should only see my own reviews, never someone elses
    @Test
    void list_showsOnlyMyOwnReviews() throws Exception {
        repository.save(Restaurant.wantToVisit(otherUser(), "Eriks place", LAT, LON));

        HttpResponse<String> response = get("/api/restaurants");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).doesNotContain("Eriks place");
    }

    // trying to remove someone elses review should give 403 and remove nothing
    @Test
    void deleteSomeoneElsesReview_returns403() throws Exception {
        Restaurant theirs = repository.save(Restaurant.wantToVisit(otherUser(), "Eriks place", LAT, LON));

        HttpResponse<String> response = delete("/api/restaurants/" + theirs.getId());

        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(repository.count()).isEqualTo(1);
    }

    // the user list should count visited and want to go per person
    @Test
    void userList_countsVisitedAndWantPerUser() throws Exception {
        // erik wants to go somewhere, i have visited one place
        repository.save(Restaurant.wantToVisit(otherUser(), "Eriks place", LAT, LON));
        post("""
                {
                  "placeId": "node/1", "name": "Ekstedt",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "VISITED",
                  "foodRating": 9, "atmosphereRating": 8, "valueRating": 6
                }
                """);

        HttpResponse<String> response = get("/api/users");
        assertThat(response.statusCode()).isEqualTo(200);

        // pick out one person from the json list by username
        List<Integer> myVisited = JsonPath.read(response.body(), "$[?(@.username == 'svent')].visitedCount");
        List<Integer> myWant = JsonPath.read(response.body(), "$[?(@.username == 'svent')].wantCount");
        List<Integer> eriksVisited = JsonPath.read(response.body(), "$[?(@.username == 'erik')].visitedCount");
        List<Integer> eriksWant = JsonPath.read(response.body(), "$[?(@.username == 'erik')].wantCount");

        assertThat(myVisited).containsExactly(1);
        assertThat(myWant).containsExactly(0);
        assertThat(eriksVisited).containsExactly(0);
        assertThat(eriksWant).containsExactly(1);
    }

    // only places that two or more of us want to visit show up in go together
    @Test
    void together_listsPlacesThatTwoOrMoreWantToVisit() throws Exception {
        // both erik and i want to go here, so it should show up
        post(wantJson("node/1", "Shared place"));
        Restaurant eriksWant = Restaurant.wantToVisit(otherUser(), "Shared place", LAT, LON);
        eriksWant.setPlaceId("node/1");
        repository.save(eriksWant);

        // only i want to go here, so it should not show up
        post(wantJson("node/2", "Only mine"));

        // i want to go, but erik has already been, so it is not a shared wish
        post(wantJson("node/3", "Erik has been"));
        Restaurant eriksVisit = Restaurant.visited(otherUser(), "Erik has been", LAT, LON, 8, 8, 8, null);
        eriksVisit.setPlaceId("node/3");
        repository.save(eriksVisit);

        HttpResponse<String> response = get("/api/restaurants/together");

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).containsExactly("Shared place");

        // the names come sorted, from the ORDER BY inside STRING_AGG
        String wantBy = JsonPath.read(response.body(), "$[0].wantBy");
        assertThat(wantBy).isEqualTo("Erik, Jeff");
    }

    // my taste profile: average per kind of food, only my own visits, best first
    @Test
    void taste_givesMyAveragePerCategory_onlyFromMyVisits() throws Exception {
        // the place ids start with taste- so this test can clean up only its own places
        jdbc.update("DELETE FROM place WHERE id LIKE 'taste-%'");
        insertPlace("taste-1", "Pizza One", "pizza_restaurant");
        insertPlace("taste-2", "Pizza Two", "pizza_restaurant");
        insertPlace("taste-3", "Sushi Place", "sushi_restaurant");
        insertPlace("taste-4", "Thai Wish", "thai_restaurant");
        insertPlace("taste-5", "Eriks Cafe", "cafe");

        // two pizza places (9 and 7, so 8.0 on average) and one sushi place (6)
        post(visitedJson("taste-1", "Pizza One", 9));
        post(visitedJson("taste-2", "Pizza Two", 7));
        post(visitedJson("taste-3", "Sushi Place", 6));

        // only on my wishlist, no ratings, so it should not count
        post(wantJson("taste-4", "Thai Wish"));

        // erik visited a cafe, but this is my profile, so it should not count
        Restaurant eriksVisit = Restaurant.visited(otherUser(), "Eriks Cafe", LAT, LON, 10, 10, 10, null);
        eriksVisit.setPlaceId("taste-5");
        repository.save(eriksVisit);

        HttpResponse<String> response = get("/api/restaurants/taste");

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> categories = JsonPath.read(response.body(), "$[*].category");
        assertThat(categories).containsExactly("pizza_restaurant", "sushi_restaurant");

        Double pizzaScore = JsonPath.read(response.body(), "$[0].averageScore");
        Integer pizzaVisits = JsonPath.read(response.body(), "$[0].visits");
        assertThat(pizzaScore).isEqualTo(8.0);
        assertThat(pizzaVisits).isEqualTo(2);
    }

    // the friends feed: the newest three from each friend, never my own reviews
    @Test
    void activity_showsNewestThreeFromEachFriend_neverMine() throws Exception {
        // my own review is the newest of all, but it should never show up in my friends feed
        post(wantJson("node/9", "My own place"));

        // erik has four reviews, place 1 is the oldest and place 4 the newest
        for (int i = 1; i <= 4; i++) {
            Restaurant review = repository.save(Restaurant.wantToVisit(otherUser(), "Erik place " + i, LAT, LON));
            // set the time by hand so the order is always the same, place 1 is 9 minutes old, place 4 is 6
            jdbc.update("UPDATE restaurant SET updated_at = now() - make_interval(mins => ?) WHERE id = ?",
                    10 - i, review.getId());
        }

        HttpResponse<String> response = get("/api/restaurants/activity");

        assertThat(response.statusCode()).isEqualTo(200);

        // only the three newest from erik, newest first, and the oldest one is left out
        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).containsExactly("Erik place 4", "Erik place 3", "Erik place 2");
    }


    // top places: at least 2 visited reviews, best score first, more reviews wins a tie
    @Test
    void top_ranksByScoreThenReviews_needsTwoReviews() throws Exception {
        AppUser erik = otherUser();
        AppUser anna = anna();

        // 9.0 from two reviews, should be number 1
        post(visitedJson("top-1", "Best", 9));
        saveVisited(erik, "top-1", "Best", 9);

        // 8.0 from three reviews, beats the other 8.0 because more people agree
        post(visitedJson("top-2", "Popular", 8));
        saveVisited(erik, "top-2", "Popular", 8);
        saveVisited(anna, "top-2", "Popular", 8);

        // 8.0 from two reviews, number 3
        post(visitedJson("top-3", "Also eight", 8));
        saveVisited(erik, "top-3", "Also eight", 8);

        // a perfect score but only one review, so it should not be ranked
        post(visitedJson("top-4", "Only one", 10));

        // erik visited and i only want to go, a wish is not a review, so still only one
        post(wantJson("top-5", "Wished"));
        saveVisited(erik, "top-5", "Wished", 10);

        HttpResponse<String> response = get("/api/restaurants/top");

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> names = JsonPath.read(response.body(), "$[*].name");
        List<Integer> ranks = JsonPath.read(response.body(), "$[*].rank");
        assertThat(names).containsExactly("Best", "Popular", "Also eight");
        assertThat(ranks).containsExactly(1, 2, 3);

        Double bestScore = JsonPath.read(response.body(), "$[0].averageScore");
        Integer popularReviews = JsonPath.read(response.body(), "$[1].reviewCount");
        assertThat(bestScore).isEqualTo(9.0);
        assertThat(popularReviews).isEqualTo(3);
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

    // another user with reviews of their own, created once and reused
    private AppUser otherUser() {
        return users.findByUsername("erik")
                .orElseGet(() -> users.save(new AppUser("erik", "Erik", "not used", Role.FRIEND)));
    }

    private HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("X-XSRF-TOKEN", csrfToken())
                .DELETE()
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // json for saving a place as want to visit
    private String wantJson(String placeId, String name) {
        return """
                { "placeId": "%s", "name": "%s", "latitude": 59.33, "longitude": 18.06, "status": "WANT_TO_VISIT" }
                """.formatted(placeId, name);
    }

    // json for saving a place as visited, with the same rating for all three
    private String visitedJson(String placeId, String name, int rating) {
        return """
                { "placeId": "%s", "name": "%s", "latitude": 59.33, "longitude": 18.06, "status": "VISITED",
                  "foodRating": %d, "atmosphereRating": %d, "valueRating": %d }
                """.formatted(placeId, name, rating, rating, rating);
    }

    // a place in the place table, kind is filled in by postgres
    private void insertPlace(String id, String name, String category) {
        jdbc.update("""
                INSERT INTO place (id, name, category, latitude, longitude, imported_at)
                VALUES (?, ?, ?, 59.33, 18.06, now())
                """, id, name, category);
    }


    // a third person, so a place can have more reviews than just erik and me
    private AppUser anna() {
        return users.findByUsername("anna")
                .orElseGet(() -> users.save(new AppUser("anna", "Anna", "not used", Role.FRIEND)));
    }

    // saves a visited review for someone else, with the same rating for all three
    private void saveVisited(AppUser user, String placeId, String name, int rating) {
        Restaurant review = Restaurant.visited(user, name, LAT, LON, rating, rating, rating, null);
        review.setPlaceId(placeId);
        repository.save(review);
    }
}