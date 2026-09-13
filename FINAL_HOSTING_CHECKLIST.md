# Ampara Tourism — Final Hosting Checklist

## Before deployment
- [ ] Run `mvn -B clean verify` in GitHub Actions/CI — this sandbox has no Maven/network, so the CI workflow (`.github/workflows/ci.yml`) or Render's own build is the first place this actually runs. Do not merge/deploy on a red build.
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`.
- [ ] Set `APP_CORS_ALLOWED_ORIGINS` to the **exact** deployed frontend origin(s) only — e.g. `https://ampara-tourism.example` (comma-separate multiple, no trailing slash, no `*`). `allowCredentials(true)` in `ProductionSecurityConfig` means a wildcard origin will be rejected by browsers anyway, but a mismatched/missing value will silently break auth from the real frontend, so verify it after deploy, not just before.
- [ ] Ensure `JWT_SECRET` and `ADMIN_PASSWORD` are generated/strong production secrets — Render's `generateValue: true` in `render.yaml` already does this for both; don't override them with weak values in the dashboard.
- [ ] Configure PostgreSQL with `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` — `render.yaml` wires these from the managed `ampara-tourism-db` automatically via `fromDatabase`; only needed manually for other hosts.
- [ ] Put every real credential — DB password, `JWT_SECRET`, `ADMIN_PASSWORD`, `OPENAI_API_KEY`, `WEATHER_API_KEY`, `GOOGLE_MAPS_API_KEY`, `CLOUDINARY_*`, `FIREBASE_CREDENTIALS_BASE64`, mail credentials — in Render's Secret/Environment manager, never in `render.yaml`, `application*.yml`, or git. All of these are already read from env vars in the code; none are hardcoded.
- [ ] Decide whether Swagger should ever be reachable in production. It's **off by default in prod** now (`springdoc.api-docs.enabled` / `swagger-ui.enabled` default to `false` via `SWAGGER_ENABLED`), since it otherwise exposes the entire API surface — including admin routes — to anyone. Set `SWAGGER_ENABLED=true` only temporarily if a teammate needs it, then unset it.
- [ ] Verify the frontend uses the deployed backend API base URL.

## After deployment
- [ ] `GET /actuator/health` returns UP.
- [ ] Home page loads with no 404s for CSS/JS/assets.
- [ ] Login/register works.
- [ ] Tourist places and town filtering work.
- [ ] Nearby hotels/rooms/food work.
- [ ] Food names containing apostrophes render safely.
- [ ] Booking rejects invalid dates and prevents overlapping room bookings.
- [ ] Weather/API failures fall back without taking down the application.
- [ ] Google Maps failures fall back to navigation links.
- [ ] Admin endpoints reject non-admin users.

## Production hosting note
`render.yaml` now sets a paid plan on both the web service (`starter`) and the database
(`basic-256mb`) instead of `free`. Confirm automated Postgres backups are enabled in the
Render dashboard after the first deploy on the paid plan.

## Schema management note (ddl-auto / Flyway)
`spring.jpa.hibernate.ddl-auto` is now `validate` in `application-prod.yml` (it was
`update`, which silently alters the live schema on every boot). Flyway is now wired in
with `baseline-on-migrate: true`, which adopts your existing database's current schema
as "version 0" on first boot after this change, without altering it. **Read
`src/main/resources/db/migration/README.md` before your next deploy** — it explains a
required one-time check on staging, and a different bootstrap step if you are ever
deploying to a brand-new, empty database rather than your existing one. This part could
not be tested in the sandbox that produced this change (no Maven/network access), so
verify it on a staging copy of the database first.
