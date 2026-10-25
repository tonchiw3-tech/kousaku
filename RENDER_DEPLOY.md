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
| `SPRING_PROFILES_ACTIVE` | `render` |
| `STAFF_USERNAME` | Staff login username |
| `STAFF_PASSWORD` | Staff login password |

The `render` profile uses an in-memory H2 database and applies
`schema-render.sql` on startup. It is intended for the submission demo only;
data is not persistent and no H2 console is exposed. Do not set the MySQL
`DB_URL`, `DB_USERNAME`, or `DB_PASSWORD` variables for this profile.

## Verification

Run the following from the repository root before pushing:

```powershell
.\mvnw.cmd test
docker build -t kousaku-render-test .
docker run --rm -p 10000:10000 `
  -e SPRING_PROFILES_ACTIVE=render `
  -e STAFF_USERNAME="..." -e STAFF_PASSWORD="..." `
  kousaku-render-test
```

Then open `http://localhost:10000/`. To smoke-test the service in Render, open
the service's public URL and create a reservation or inquiry through the UI.
