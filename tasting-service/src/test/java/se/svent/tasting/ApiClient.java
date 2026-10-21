package se.svent.tasting;

import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// a small http client for tests that behaves like a browser
// it keeps cookies between requests and sends the csrf token when changing something
public class ApiClient {

    private final int port;
    private final CookieManager cookies = new CookieManager();
    private final HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();

    public ApiClient(int port) {
        this.port = port;
    }

    public HttpResponse<String> login(String username, String password) throws Exception {
        return post("/api/auth/login", """
                { "username": "%s", "password": "%s" }
                """.formatted(username, password));
    }

    public HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(url(path)).GET().build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> post(String path, String json) throws Exception {
        // a fresh client has no csrf cookie yet, any get request makes spring hand one out
        if (csrfToken().isEmpty()) {
            get("/api/auth/me");
        }
        HttpRequest request = HttpRequest.newBuilder(url(path))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return http.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // finds the csrf token spring put in the cookie jar
    private String csrfToken() {
        return cookies.getCookieStore().getCookies().stream()
                .filter(c -> c.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElse("");
    }

    private URI url(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}