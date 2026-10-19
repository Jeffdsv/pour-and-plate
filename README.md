# Pour & Plate

I love food, beer and wine, and I brew my own beer. Whenever friends, family or I find a good restaurant, wine or beer, I want an easy way to remember it and share it. I also want to keep track of new places I'd like to go.

Most apps are pretty bad at finding hidden gems. So the idea is simple: a map that shows restaurants with their average score, my own recommendations, and the places I want to visit.

## Status

Work in progress. Right now the backend is done: an API where I can save restaurants as visited (rated on food, atmosphere and value for money, 1 to 10) or as want to visit. My score is the average of the three.

Next up is the web app with the map.

## Tech stack

- Java 25, Spring Boot, Maven
- PostgreSQL with Flyway migrations
- Docker and Docker Compose
- JUnit, Mockito and Testcontainers for testing

Planned: React frontend with a map, a C# service for statistics, Jenkins, Kubernetes.

## Running locally

You need Java 25 and Docker.

```bash
docker compose up -d
cd tasting-service
./mvnw spring-boot:run
```

The API runs on `http://localhost:8081`.

## API

| Method | URL | What it does |
| --- | --- | --- |
| `GET` | `/api/restaurants` | All restaurants |
| `GET` | `/api/restaurants?status=VISITED` | Only visited (or `WANT_TO_VISIT`) |
| `GET` | `/api/restaurants/{id}` | One restaurant |
| `POST` | `/api/restaurants` | Save one. Same `osmId` again updates it |
| `DELETE` | `/api/restaurants/{id}` | Delete |

Example:

```bash
curl -X POST http://localhost:8081/api/restaurants \
  -H "Content-Type: application/json" \
  -d '{
    "osmId": "node/1",
    "name": "Ekstedt",
    "latitude": 59.3389,
    "longitude": 18.0730,
    "status": "VISITED",
    "foodRating": 9,
    "atmosphereRating": 8,
    "valueRating": 6
  }'
```

Gives back the restaurant with `"myScore": 7.7`.

## Tests

```bash
cd tasting-service
./mvnw test
```

There are unit tests for the rating rules, service tests with Mockito, and integration tests that start a real PostgreSQL in Docker with Testcontainers and send real HTTP requests to the app. Docker needs to be running.

## What I learned so far

- The hardest part was narrowing down exactly what I wanted to build. I started with wine, beer and restaurants all at once, and ended up focusing on restaurants first.
- I like errors. Sitting with a problem until I understand why it happens is how I learn the most.
- (add a concrete example here later, like an error you solved)

## Next steps

- Web app with a map, my own pins, and highlighted want to visit places
- Google rating shown next to my own score
- Statistics with my own SQL queries
- Wine and beer pairing suggestions
- CI/CD with Jenkins, then Kubernetes

## Author

 [GitHub](https://github.com/Jeffdsv)