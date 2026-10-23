# Render deployment

This repository is configured as a Docker-based Spring Boot service. The image
builds with the Maven Wrapper and runs the packaged application on port `10000`.

## Render Web Service settings

- Runtime: **Docker**
- Root Directory: repository root (leave blank)
- Dockerfile Path: `./Dockerfile`
- Health-check path: `/`

Set these environment variables in Render. Do not commit their values.

| Variable | Value |
| --- | --- |
| `DB_URL` | JDBC URL for a reachable managed MySQL database, for example `jdbc:mysql://HOST:3306/DATABASE?useSSL=true&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci` |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `STAFF_USERNAME` | Staff login username |
| `STAFF_PASSWORD` | Staff login password |

`spring.sql.init.mode=always` applies `schema.sql` on startup. The database user
must be permitted to create or alter the application's tables. Use a managed
MySQL service reachable from Render; a MySQL server running only on a developer
PC cannot be reached by Render.

## Verification

Run the following from the repository root before pushing:

```powershell
.\mvnw.cmd test
docker build -t kousaku-render-test .
docker run --rm -p 10000:10000 `
  -e DB_URL="jdbc:mysql://HOST:3306/DATABASE?useSSL=true&serverTimezone=Asia/Tokyo" `
  -e DB_USERNAME="..." -e DB_PASSWORD="..." `
  -e STAFF_USERNAME="..." -e STAFF_PASSWORD="..." `
  kousaku-render-test
```

Then open `http://localhost:10000/`. To smoke-test the service in Render, open
the service's public URL and create a reservation or inquiry through the UI.
