package se.svent.tasting.restaurant;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import se.svent.tasting.TestcontainersConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

// starts the whole app on a random port with a real postgres in docker
// sends real http requests to it, like the app would
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class RestaurantApiIntegrationTest {

    // the random port spring picked
    @LocalServerPort
    private int port;

    // the real repository, used to check and clean the database
    @Autowired
    private RestaurantRepository repository;

    // plain java http client, no extra library needed
    private final HttpClient http = HttpClient.newHttpClient();


    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }


    @Test
    void saveVisited_thenFetchById_returnsScore() throws Exception {
        HttpResponse<String> saved = post("""
                {
                  "osmId": "node/1", "name": "Ekstedt",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "VISITED",
                  "foodRating": 9, "atmosphereRating": 8, "valueRating": 6
                }
                """);
        assertThat(saved.statusCode()).isEqualTo(200);

        // $.id means the id field
        Integer id = JsonPath.read(saved.body(), "$.id");

        HttpResponse<String> fetched = get("/api/restaurants/" + id);
        assertThat(fetched.statusCode()).isEqualTo(200);
        Double score = JsonPath.read(fetched.body(), "$.myScore");
        assertThat(score).isEqualTo(7.7);
    }

    // same place twice should be one row in the real database
    @Test
    void saveSameOsmIdTwice_keepsOneRow() throws Exception {
        String json = """
                {
                  "osmId": "node/1", "name": "Ekstedt",
                  "latitude": 59.33, "longitude": 18.06,
                  "status": "WANT_TO_VISIT"
                }
                """;
        post(json);
        post(json);

        assertThat(repository.count()).isEqualTo(1);
    }

    // bad data should give 400 message and save nothing
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

    @Test
    void unknownId_returns404() throws Exception {
        HttpResponse<String> response = get("/api/restaurants/999");

        assertThat(response.statusCode()).isEqualTo(404);
    }

    // helpers that send requests to the running app4

    private HttpResponse<String> post(String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url("/api/restaurants")))
                .header("Content-Type", "application/json")
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
