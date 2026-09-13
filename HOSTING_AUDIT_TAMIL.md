# Hosting Audit — Final Check

## Critical fixes applied in this corrected package

1. **Render Blueprint path fixed**
   - Added a root-level `render.yaml`.
   - Render now uses `rootDir: ./tourism-service`, so the Dockerfile and `pom.xml` are found correctly.
2. **Spring component scanning fixed**
   - The external integration and production CORS classes were under `com.tourism.*`, outside the application's `com.ampara.tourism` component-scan root.
   - Moved them to `com.ampara.tourism.integration` and `com.ampara.tourism.security`.
   - This ensures `/api/integrations/**` and the production CORS configuration are actually registered.
3. **Admin email secret handling**
   - Root Render blueprint asks for `ADMIN_EMAIL` as an environment value instead of embedding a demo address.

## Verification performed

- ZIP structure inspected: 196 original archive entries.
- All frontend HTML local asset references checked; no genuine missing static assets found.
- All JavaScript files under the static frontend pass `node --check`.
- Existing security/XSS/N+1/coordinate/nearby-room fixes were retained.
- Full Maven build could not be executed in this environment because Maven is not installed. Render/CI must run `mvn -B clean verify` before production traffic.

## Remaining deployment requirements

Before going live, set:
- `ADMIN_EMAIL`
- `APP_CORS_ALLOWED_ORIGINS` (exact frontend origin(s), if frontend is hosted separately)
- `JWT_SECRET` (Render generates it automatically)
- `ADMIN_PASSWORD` (Render generates it automatically)
- Any actually-used integration secrets: OpenAI, Weather, Google Maps, Cloudinary, Firebase, and mail.

After deployment test:
- `/actuator/health`
- `/`
- `/auth.html`
- `/map.html`
- `/events.html`
- `/api/places`
- `/api/integrations/weather`
- login and an admin endpoint
- booking flow

**Important:** the seeded tourism dataset contains placeholder entries as documented in the project README; verify real-world attraction details before public launch.
