# Multiplication Trainer

A practice application for multiplication and division exercises. Enter the participant name, choose an operation and a number from 1 to 10, then solve 10 tasks one by one. You can enable a 2, 3, or 5 minute timer, or practice without any time limit. At the end, the correct and incorrect answers are shown for each task.

## Running with Docker

Docker Desktop and Docker Compose are required.

```powershell
docker compose up --build
```

Open `http://localhost:8080`. The Spring API is available at `http://localhost:8081`, and PostgreSQL data is stored in the Docker volume `postgres_data`.

To stop it, use `Ctrl+C`, then:

```powershell
docker compose down
```

## Local Development

Node.js 22+, Java 21+, Maven, and PostgreSQL are required.

1. Start PostgreSQL with the database `multiplication_db`, user `trainer`, and password `trainer`.
2. In `backend/`, run `mvn spring-boot:run`.
3. In `frontend/`, run `npm install`, then `npm start`.
4. Open `http://localhost:4200`. The Angular proxy forwards `/api` to `localhost:8080`.

## Tests

In `frontend/`, run `npm test` for Angular unit tests. In `backend/`, run `mvn test` for Spring service unit tests.

## API

- `POST /api/sessions` accepts `participantName`, `multiplier`, `operation` (`MULTIPLICATION` or `DIVISION`), `mode` (`TABLE` or `MIXED`), and `timeLimitMinutes` (`null`, `2`, `3`, or `5`). It returns the session and the tasks with the specific operands.
- `POST /api/sessions/{id}/submit` accepts answers for factors from 1 to 10. An empty response is sent as `null` and is counted as incorrect.
- The result contains the number of correct and incorrect answers and the correct answer for each task.