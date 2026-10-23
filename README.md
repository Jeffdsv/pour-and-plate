# Pour & Plate

**Live demo:** **Live demo:** https://pourandplate.se

> The app a scales to zero when nobody is using it, so the first visit can take up to a minute while it wakes up. Accounts are invite only, so ask me for a demo login.

I love food, beer and wine, and I brew my own beer. Whenever friends, family or I find a good restaurant, wine or beer, I want an easy way to remember it and share it. I also want to keep track of new places I'd like to go.

Most apps are pretty bad at finding hidden gems. So the idea is simple: a map that shows restaurants with their average score, my own recommendations, and the places I want to visit.

## What it does

- A map of all food and drink places in Sweden (32,000+ from Overture Maps), grouped into bubbles that split up as you zoom in
- Click a place to save it as want to visit, or as visited with a rating for food, atmosphere and value for money (1 to 10). My score is the average of the three
- Notes, the restaurant's website, and Google's own rating in a small Google Maps box
- Friends join through single use invite links that expire after 15 minutes
- A side panel with my map, every friend's map (read only) and an Everyone map that shows who has been where and the shared average score
- Works on phone and computer
- Search for any place by name, or tap Near me to see what is closest, filtered by type and distance
- Filter the map by restaurants, cafés, wine bars, beer places or bars
- Go together: places two or more of us want to visit
- My stats: my taste profile per kind of food, and the latest from my friends

## Tech stack

- **Backend:** Java 25, Spring Boot, Spring Security (session login, BCrypt, CSRF), Spring Session JDBC, Maven
- **Database:** PostgreSQL with Flyway migrations, hand written SQL for the summaries
- **Frontend:** React, TypeScript, Vite, Leaflet with a MapLibre vector map
- **Containers:** Docker multi stage builds, nginx, Docker Compose
- **Cloud:** Azure Container Apps (scales to zero), Azure Database for PostgreSQL, GitHub Container Registry
- **Tests:** JUnit, Mockito and Testcontainers
- **CI/CD:** Jenkins pipeline that runs the tests, builds both images tagged with the commit id, pushes them and deploys to Azure on every commit

Planned: a C# service for statistics, Jenkins, Kubernetes.

## SQL highlights

All queries are hand written native SQL in Spring Data `@Query`, each with an integration test against a real PostgreSQL in Testcontainers.

| Feature       | What it does | SQL idea |
|---------------| --- | --- |
| Search      | Find any of the 32,000 places by part of its name, best match first | `pg_trgm` extension with a GIN index, so `ILIKE '%text%'` stays fast, sorted by `similarity()` ([V12](tasting-service/src/main/resources/db/migration/V12__place_name_search.sql), [query](tasting-service/src/main/java/se/svent/tasting/place/PlaceRepository.java)) |
| Map filter  | Restaurants, cafés, wine bars, beer places and bars | A `GENERATED ALWAYS AS (CASE WHEN ...) STORED` column works out each place's kind from its category ([V11](tasting-service/src/main/resources/db/migration/V11__place_kind.sql)) |
| Near me       | The closest places within 500 m to 2 km, with the distance in meters | Haversine formula in a derived table, after a cheap bounding box that uses the index ([query](tasting-service/src/main/java/se/svent/tasting/place/PlaceRepository.java)) |
| Go together   | Places two or more of us want to visit | `GROUP BY` with `HAVING COUNT(*) >= 2` and `STRING_AGG` for the names ([query](tasting-service/src/main/java/se/svent/tasting/restaurant/RestaurantRepository.java)) |
| Taste profile | My average score per kind of food | `JOIN` between my reviews and the places, `AVG` and `ROUND` per category ([query](tasting-service/src/main/java/se/svent/tasting/restaurant/RestaurantRepository.java)) |
| Latest from friends | The newest three reviews from each friend | Window function `ROW_NUMBER() OVER (PARTITION BY user ORDER BY updated_at DESC)` for top N per group ([query](tasting-service/src/main/java/se/svent/tasting/restaurant/RestaurantRepository.java)) |
| Everyone map  | Each place once, with who visited, who wants to go and the shared score | `COUNT(*) FILTER (WHERE ...)`, `AVG`, `STRING_AGG` ([query](tasting-service/src/main/java/se/svent/tasting/restaurant/RestaurantRepository.java)) |

## Run locally

You need Docker Desktop. Create a `.env` file next to `docker-compose.yml`:

```
ADMIN_PASSWORD=choose-a-password
VITE_GOOGLE_MAPS_KEY=optional-google-embed-key
```

Then start everything:

```
docker compose --profile app up -d --build
```

Open http://localhost:8080 and log in as `svent` with your admin password.

For development, `docker compose up -d` starts only the database. Then run the backend from IntelliJ (port 8081) and the frontend with `npm run dev` in `frontend` (port 5173).

## API

Everything except login and invites needs a logged in session. Requests that change something also need the `X-XSRF-TOKEN` header.

| Method | URL | What it does |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Log in |
| `GET` | `/api/auth/me` | Who is logged in |
| `GET` | `/api/places?south=&west=&north=&east=&kinds=` | Places inside the visible map area, only the chosen kinds |
| `GET` | `/api/restaurants` | My saved places (`?status=VISITED` or `WANT_TO_VISIT`) |
| `GET` | `/api/restaurants/{id}` | One of my saved places |
| `GET` | `/api/places/search?q=` | Search places by name |
| `GET` | `/api/places/near?lat=&lon=&radius=&kinds=` | The closest places, with distance in meters |
| `GET` | `/api/restaurants/together` | Places two or more of us want to visit |
| `GET` | `/api/restaurants/taste` | My average ratings per kind of food |
| `GET` | `/api/restaurants/activity` | The newest reviews from my friends |
| `POST` | `/api/restaurants` | Save or rate a place. Same `placeId` again updates it |
| `DELETE` | `/api/restaurants/{id}` | Remove one of mine (403 if it is someone else's) |
| `GET` | `/api/restaurants/summary` | Every saved place combined, with counts and average score |
| `GET` | `/api/users` | Everyone, with how many places they visited and want to visit |
| `GET` | `/api/users/{id}/restaurants` | A friend's map, read only |
| `POST` | `/api/invites` | Create an invite link (admin only) |
| `POST` | `/api/invites/{token}/accept` | Create an account from an invite |

## Tests

```bash
cd tasting-service
./mvnw test
```

There are unit tests for the rating rules, service tests with Mockito, and integration tests that start a real PostgreSQL in Docker with Testcontainers and send real HTTP requests to the app, logged in with CSRF like a browser. Docker needs to be running.

## What I learned so far

- The hardest part was narrowing down exactly what I wanted to build. I started with wine, beer and restaurants all at once, and ended up focusing on restaurants first.
- I like errors. Sitting with a problem until I understand why it happens is how I learn the most.
- (add a concrete example here later, like an error you solved)
- Getting the app online took a lot longer than I expected. Pushing my Docker images to GitHub's container registry kept failing with "denied", and once the app was on Azure I couldn't log in at all. After a couple of hours of reading logs I found the cause: nginx was still calling the backend on the wrong port.
- Some bugs only showed up in special cases. A popup near the top of the map got stuck half see-through, and it turned out React was giving it a "new" position on every redraw, which made the map reopen it over and over.
- I learned the hard way that you never edit a database migration after it has run. I changed the formatting of an old Flyway file, and the app refused to start until I understood why.
- Every new idea meant going back and changing code I had already written. I spent a lot of time trying to keep the code clean and not repeat myself, for example by making small components that the popups could share.

## Next steps

- Own domain
- Statistics with my own SQL queries
- Wine and beer recommendations, and beer and wine places I recommend
- CI/CD with Jenkins, then Kubernetes

## Author

[GitHub](https://github.com/Jeffdsv)