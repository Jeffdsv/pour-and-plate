package se.svent.tasting.place;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import se.svent.tasting.ApiClient;
import se.svent.tasting.TestcontainersConfiguration;

import java.net.http.HttpResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// starts the whole app with a real postgres and tests the place endpoints over http
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.admin.password=test-password")
@Import(TestcontainersConfiguration.class)
class PlaceApiIntegrationTest {

    @LocalServerPort
    private int port;

    // place is read only in the app, so the test writes its own rows with plain sql
    @Autowired
    private JdbcTemplate jdbc;

    private ApiClient api;

    // every test starts with the same three places and a logged in user
    @BeforeEach
    void setUp() throws Exception {
        jdbc.update("DELETE FROM place");
        insertPlace("p1", "Ekstedt", "scandinavian_restaurant");
        insertPlace("p2", "Lekstugan", "cafe");
        insertPlace("p3", "Pizza Hut", "pizza_restaurant");

        api = new ApiClient(port);
        api.login("svent", "test-password");
    }

    // names that contain the text come back, the closest match first
    @Test
    void search_findsNamesContainingTheText_bestMatchFirst() throws Exception {
        HttpResponse<String> response = api.get("/api/places/search?q=ekst");

        assertThat(response.statusCode()).isEqualTo(200);
        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).containsExactly("Ekstedt", "Lekstugan");
    }

    // the kind comes from the generated column in V11
    @Test
    void search_returnsTheKindPostgresWorkedOut() throws Exception {
        HttpResponse<String> response = api.get("/api/places/search?q=ekstedt");

        String kind = JsonPath.read(response.body(), "$[0].kind");
        assertThat(kind).isEqualTo("RESTAURANT");
    }

    // one letter would match almost everything, so it gives an empty list
    @Test
    void search_withOneLetter_returnsNothing() throws Exception {
        HttpResponse<String> response = api.get("/api/places/search?q=e");

        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).isEmpty();
    }

    // a % is searched as a normal sign, not as the any text wildcard
    @Test
    void search_percentSign_isNotAWildcard() throws Exception {
        // %25 is how a % is written inside a web address
        HttpResponse<String> response = api.get("/api/places/search?q=%25%25");

        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).isEmpty();
    }

    // near me only gives places of the chosen kind inside the radius, with the distance in meters
    @Test
    void near_findsCloseBeerPlaces_withDistance() throws Exception {
        // 0.0009 degrees north is about 100 meters, 0.009 degrees is about 1 km
        insertPlaceAt("p4", "Close pub", "pub", 59.3309, 18.06);
        insertPlaceAt("p5", "Far pub", "pub", 59.3390, 18.06);
        insertPlaceAt("p6", "Close cafe", "cafe", 59.3305, 18.06);

        HttpResponse<String> response = api.get("/api/places/near?lat=59.33&lon=18.06&radius=500&kinds=BEER");

        assertThat(response.statusCode()).isEqualTo(200);

        // the far pub is outside 500 m and the cafe is not a beer place
        List<String> names = JsonPath.read(response.body(), "$[*].name");
        assertThat(names).containsExactly("Close pub");

        // haversine should give about 100 meters
        Double distance = JsonPath.read(response.body(), "$[0].distanceMeters");
        assertThat(distance).isBetween(95.0, 105.0);
    }

    // a radius bigger than 5 km is refused
    @Test
    void near_withTooBigRadius_returns400() throws Exception {
        HttpResponse<String> response = api.get("/api/places/near?lat=59.33&lon=18.06&radius=50000");

        assertThat(response.statusCode()).isEqualTo(400);
    }


    // helper: one place in the middle of stockholm, kind is filled in by postgres
    private void insertPlace(String id, String name, String category) {
        jdbc.update("""
                INSERT INTO place (id, name, category, latitude, longitude, imported_at)
                VALUES (?, ?, ?, 59.33, 18.06, now())
                """, id, name, category);
    }

    // helper: a place at an exact position, for testing distances
    private void insertPlaceAt(String id, String name, String category, double lat, double lon) {
        jdbc.update("""
                INSERT INTO place (id, name, category, latitude, longitude, imported_at)
                VALUES (?, ?, ?, ?, ?, now())
                """, id, name, category, lat, lon);
    }
}
