# Ampara Tourism — Production Hardening Update

This version focuses on high-traffic resilience without introducing a mandatory Redis/Kubernetes dependency.

## Implemented

- Bounded Caffeine cache for external integration responses (prevents unbounded memory growth).
- 60-second bounded cache for Nearby Services responses.
- Nearby Services now uses database bounding-box prefilters instead of loading every hotel/food row into the JVM.
- Runtime town-level food coordinate fallback remains supported.
- Batch room loading remains in place (`findByHotelIdIn`).
- Database indexes added for hotel/food coordinates, town, room-to-hotel, and booking room/date lookups.
- HikariCP production pool limits and connection timeouts configured.
- `spring.jpa.open-in-view=false` for safer production DB usage.
- External API connect/read timeouts prevent slow providers from consuming request threads indefinitely.
- Lightweight per-instance API rate limiting: 120 requests/minute and 20 auth requests/minute by default.
- Graceful application shutdown enabled.
- Booking creation is transactional and locks the selected room row while checking overlapping confirmed bookings, preventing concurrent double-booking for the same room/date range.
- Existing XSS, N+1, coordinate fallback, and nearby-room UI fixes are retained.

## Production deployment recommendations

1. Use a managed PostgreSQL database with automated backups and point-in-time recovery if available.
2. Set strong values for `JWT_SECRET` and `ADMIN_PASSWORD` in the deployment secret store.
3. Set `APP_CORS_ALLOWED_ORIGINS` to the real frontend origin(s), not `*`.
4. Configure Google Maps, Meteorology, Cloudinary, email, and OpenAI credentials only as server-side secrets.
5. Put a CDN/WAF/reverse proxy in front of the service when traffic grows. Use its global rate limiting for multi-instance deployments.
6. For horizontal scaling, use 2+ application instances behind a load balancer. The application is stateless; do not depend on the local in-process cache for correctness.
7. Monitor `/actuator/health`, HTTP 5xx rate, latency, CPU, memory, and PostgreSQL connections.
8. Keep image traffic on Cloudinary/CDN rather than the Spring Boot process.
9. For very large geographic datasets, migrate from bounding-box + Haversine filtering to PostgreSQL PostGIS.

## Important capacity note

The app is hardened for a much larger visitor load than the original single-instance design, but no application can guarantee unlimited concurrent users. The next scaling step is infrastructure-level: WAF/CDN + load balancer + multiple instances + managed PostgreSQL sizing/replicas.


## Final hosting readiness notes

- Render's free web service is suitable for demos/testing, not guaranteed high-availability production traffic. For real tourist traffic, use a paid always-on instance and managed PostgreSQL with backups.
- This service is stateless at the application layer; when scaling to multiple instances, use a shared PostgreSQL/Redis layer and enforce global rate limits at the CDN/WAF/load-balancer.
- Production database configuration now uses `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`, avoiding the common `postgresql://` vs `jdbc:postgresql://` URL mismatch.
- Never deploy with the development JWT/admin fallback values. Render `generateValue` should be retained for `JWT_SECRET` and `ADMIN_PASSWORD`.
- Set `APP_CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin(s), comma-separated; do not use `*` when credentials are enabled.
- Configure `OPENAI_API_KEY`, Google Maps, weather, Cloudinary and mail credentials only as hosting secrets.
- Run `mvn -B clean verify` in CI before deployment.
- After deployment, verify `/actuator/health`, homepage, login, nearby services, booking, and admin endpoints.
