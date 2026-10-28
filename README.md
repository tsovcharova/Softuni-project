# Умножител

Приложение за упражнения по умножение и деление. Въведи име на участника, избери операция и число от 1 до 10, след което решавай 10 задачи една по една. Можеш да включиш таймер от 2, 3 или 5 минути или да упражняваш без ограничение във времето. Накрая се показват верните и грешните отговори за всяка задача.

## Стартиране с Docker

Необходими са Docker Desktop и Docker Compose.

```powershell
docker compose up --build
```

Отвори `http://localhost:8080`. Spring API е достъпно на `http://localhost:8081`, а PostgreSQL данните се пазят в Docker volume `postgres_data`.

За спиране използвай `Ctrl+C`, а после:

```powershell
docker compose down
```

## Локална разработка

Необходими са Node.js 22+, Java 21+, Maven и PostgreSQL.

1. Стартирай PostgreSQL с база `multiplication_db`, потребител `trainer` и парола `trainer`.
2. В `backend/` изпълни `mvn spring-boot:run`.
3. В `frontend/` изпълни `npm install`, после `npm start`.
4. Отвори `http://localhost:4200`. Angular proxy препраща `/api` към `localhost:8080`.

## Тестове

В `frontend/` изпълни `npm test` за Angular API unit тестовете. В `backend/` изпълни `mvn test` за Spring service unit тестовете.

## API

- `POST /api/sessions` приема `participantName`, `multiplier`, `operation` (`MULTIPLICATION` или `DIVISION`), `mode` (`TABLE` или `MIXED`) и `timeLimitMinutes` (`null`, `2`, `3` или `5`). Връща сесията и задачите с конкретните операнди.
- `POST /api/sessions/{id}/submit` приема отговорите за факторите от 1 до 10. Празният отговор се изпраща като `null` и се отчита като грешен.
- Резултатът съдържа броя верни/грешни и правилния отговор за всяка задача.