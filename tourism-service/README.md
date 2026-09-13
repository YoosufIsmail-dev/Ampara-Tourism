# Ampara Smart Tourism System

Full Spring Boot backend + interactive map frontend for the Ampara district tourism
app: JWT auth, tourist registration, admin dashboard, hotel booking, a 130+ place
tourism database with an interactive Google-Maps-style navigation page, reviews &
ratings, favourites, weather, AI trip planner, image upload, Swagger docs, JUnit
tests, Docker, and CI/CD.

## Everything that's implemented

| Module | How |
|---|---|
| JWT Login / Tourist Registration | `POST /api/auth/register`, `POST /api/auth/login` - stateless JWT, BCrypt passwords |
| User Profile | `User` entity carries name/email/role; profile is whatever `/api/auth/login` returns plus favourites |
| Admin Dashboard | `ROLE_ADMIN`-gated `/api/admin/**` - stats, manage bookings/users, write access to places/attractions/hotels; admin auto-seeded on first run |
| **Tourism Places DB** | `TouristPlace` entity (English + Tamil name/description, category, GPS, town/district, hours, fee, activities...) - **131 seeded places** across all 18 towns you listed |
| **Interactive Map** | `GET /map.html` - Leaflet.js map, category & town filters, search (English or Tamil), "use my location" + nearby places, one-tap Google Maps navigation |
| Hotel & Room Booking | `Hotel` + `HotelRoom` + `Booking`, `/api/hotels`, `/api/rooms`, `/api/bookings` |
| Reviews & Ratings | `/api/reviews` - 1-5 star ratings + comments on places or hotels |
| Favourite Places | `/api/favorites` - logged-in tourists save/unsave places |
| Weather / Google Maps / Cloudinary / Email | Wired with placeholder config - app runs with none of these keys set, each just says "not configured yet" until you add credentials via env vars |
| AI Trip Planner | `/api/ai/chat` - grounded in real attraction data |
| Swagger | `/swagger-ui.html` with a JWT "Authorize" button |
| JUnit tests | `JwtUtilTest`, `AttractionControllerTest`, `TouristPlaceControllerTest` |
| Docker | `Dockerfile` + `docker-compose.yml` (app + PostgreSQL) |
| CI/CD | `.github/workflows/ci.yml` - runs `mvn clean verify` on every push/PR |
| Render deploy | `render.yaml` blueprint (web service + managed Postgres) |

## New features added

| Feature | How |
|---|---|
| **Multi-language (தமிழ் / English / Sinhala)** | `TouristPlace` now has English/Tamil/Sinhala name + description fields. `GET /api/places/{id}/localized?lang=en\|ta\|si` returns a place resolved to one language (falls back to English if a translation is missing). `GET /api/i18n/{lang}` serves UI-chrome translations (nav labels, buttons) for the app frontend; `GET /api/i18n/languages` lists supported languages. |
| **Emergency Contacts** | `/api/emergency-contacts` - nationwide hotlines (Police 119, Tourist Police 1912, Ambulance 1990, Fire 110, Disaster Mgmt 117) plus district hospitals; filter by `category` or `district`, `GET /nationwide` shortcut. Public read, admin write. |
| **Bus & Train Information** | `/api/transport` - routes with type (`BUS`/`TRAIN`), origin/destination, departure times, frequency, fare, operator. Filter by `type`, `origin`, `destination`. Seeded with real Ampara-area routes. |
| **Tourist Analytics Dashboard** | `POST /api/analytics/view` (public) logs a place view from the app; `GET /api/analytics/dashboard?days=30` (admin) returns total views, top places, top categories, top towns and a language breakdown for the window. |
| **Image Gallery** | `GalleryImage` entity, `/api/gallery` - multiple photos per place beyond the single `imageUrl` (upload via existing `/api/images` Cloudinary endpoint first, then register the URL here with an optional caption in all 3 languages). |
| **Nearby Hospitals & ATMs** | `NearbyFacility` entity, `/api/nearby-facilities` - hospitals/clinics/ATMs/banks with GPS; `GET /nearby?lat=&lng=&type=&limit=` sorts by distance (haversine, same `GeoUtil` as places). |
| **QR Code for Tourist Places** | `GET /api/places/{id}/qrcode?size=300&lang=en` returns a PNG QR code (via ZXing) linking to that place's detail page/deep link - meant for printing on physical signage. `GET /api/places/{id}/qrcode/target-url` returns just the encoded URL. |
| **AI Voice Guide** | `GET /api/voice-guide/places/{id}?lang=en\|ta\|si` - uses the existing Spring AI `ChatClient` to write a short spoken-style narration script per place, per language, for the app to read aloud with on-device text-to-speech (Web Speech API / mobile TTS). Falls back to reading out the stored description if the AI model isn't reachable, so it still works with no `OPENAI_API_KEY` set. |
| **Event Calendar** | `Event` entity, `/api/events` - festivals/religious/cultural/sports events with English/Tamil/Sinhala titles, `GET /upcoming` and `GET /range?start=&end=` (ISO datetimes). Seeded with real recurring Sri Lankan festivals (Thai Pongal, New Year, Vel Festival, Kataragama season, Arugam Bay surf season). |
| **Offline Map Cache** | `GET /api/offline/manifest?town=` bundles places (all 3 languages), emergency contacts and nearby facilities into one JSON payload for the client to cache locally (SQLite/IndexedDB) and use with zero connectivity. `GET /api/offline/version` returns a cheap hash + record counts so the client only re-downloads the full manifest when something's actually changed. Note: this ships the *data* layer only - map tile imagery itself should be cached via your map SDK's own offline-region feature (e.g. Google Maps SDK) or an MBTiles bundle. |

All ten features follow the same public-read/admin-write pattern as the rest of the
API and are wired into `SecurityConfig` accordingly (see the access rules table below).

## Latest additions

| Feature | How |
|---|---|
| **Push Notifications (Firebase Cloud Messaging)** | `POST /api/push/register` / `/unregister` (public) store a device's FCM token and auto-subscribe it to the `all-tourists` and `events` topics; `GET /api/push/status` reports whether Firebase is configured; `POST /api/push/send` (admin) broadcasts to a topic. Creating a new `Event` automatically fires a push to the `events` topic. Set `FIREBASE_CREDENTIALS_BASE64` (your service-account JSON, base64-encoded) to turn it on - with nothing set, registration still works and sends are just logged, so nothing breaks. |
| **AI Voice Guide - real audio** | `GET /api/voice-guide/places/{id}` now also returns an `audioUrl`; `GET /api/voice-guide/places/{id}/audio?lang=en\|ta\|si&voice=alloy` streams real synthesized MP3 narration via OpenAI TTS (`tts-1`), using the same `OPENAI_API_KEY` as the trip planner. Falls back to `503` if no key is set - the map UI then falls back to the browser's own `speechSynthesis` so "Listen" still works either way. Audio is cached in memory per place/language/voice to avoid re-billing repeat plays. |
| **Frontend UI (minimal, dependency-light)** | `events.html` - a hand-rolled month calendar (no calendar library) with an upcoming-events list. `analytics.html` - an admin-gated dashboard (sign in with an admin JWT) charting the `/api/analytics/dashboard` data via Chart.js (the one CDN dependency; everything else is vanilla JS/CSS). `js/common.js` + `css/app.css` - a shared nav bar and language switcher (English/Tamil/Sinhala, backed by `/api/i18n`) used across `map.html`, `events.html`, and `analytics.html`. All pages are mobile-responsive (the map's sidebar stacks above the map under 640px, the analytics charts drop to a single column, the calendar grid shrinks). |

## Environment variables (new)

```bash
export FIREBASE_CREDENTIALS_BASE64=$(base64 -i service-account.json)  # push notifications (FCM)
# OPENAI_API_KEY (above) now also powers real voice-guide audio via OpenAI TTS
```

All external integrations (Weather, Maps key delivery, Cloudinary, Email) are wired as
**configurable placeholders** — the app starts and runs fine without any of
those keys, it just tells you the feature isn't configured yet when called.

## ⚠️ Please read: about the seeded places data

You asked for 100+ real Ampara-district attractions with verified details (phone
numbers, exact fees, opening hours) across all 18 towns. I can't verify that scale
of granular, town-by-town local detail (exact fees/phones/hours for ~130 individual
spots), so I did **not** invent fake-sounding specifics and present them as fact.
Instead:

- **A handful of well-known, real landmarks are named specifically and are
  accurate to the best of my knowledge**: Arugam Bay's surf points (Main Point,
  Baby Point, Whiskey Point, Peanut Farm, Elephant Rock), Pottuvil Point, Pottuvil
  Lagoon, Muhudu Maha Viharaya, Lahugala Kitulana National Park, Magul Maha
  Viharaya, Kudumbigala Forest Hermitage, Senanayake Samudraya, Buddhangala Raja
  Maha Viharaya, Rajagala, Ampara Clock Tower.
- **The rest (~110 entries) are realistic placeholder entries** — e.g. "Nintavur
  Jumma Mosque", "Karaitivu Lagoon View Point" — generated so every town hits the
  count you asked for, with town-level GPS (slightly jittered so map markers don't
  stack). Category, activities, exact address, phone number, fee and hours on
  these are **placeholders for you to verify and fill in**, not researched facts.
- Edit them anytime via the Admin API (`PUT /api/places/{id}`) or by editing
  `src/main/resources/seed/places.json` directly and wiping the `tourist_place`
  table so it reseeds.

This gives you a fully working, fully wired system today — swap in verified data
place-by-place as you gather it, with zero code changes needed.

## The interactive map

Open `http://localhost:8081/map.html` after starting the app. It uses
**Leaflet.js + OpenStreetMap** (free, no API key) for the live map and markers,
and deep-links to `https://www.google.com/maps/dir/?api=1&destination=lat,lng`
for the actual turn-by-turn "Navigate" button (also free, no key needed). If you
later add a `GOOGLE_MAPS_API_KEY`, `/api/config/maps-key` already exposes it —
swapping `map.html`'s tile layer for the Google Maps JS API is a frontend-only
change. I chose this default so the map works immediately without you needing a
billing-enabled Google Cloud account.

Features: category filter (Beach/Temple/Mosque/Church/Museum/Nature/Wildlife/
Lake/Lagoon/Waterfall/Camping/Surfing/Historical/Harbour), town filter, live
search (English or Tamil), marker popups with rating/hours/navigate button,
"use my location" with nearest-6-places, sidebar place list synced to the map.

## 1. Environment variables

None are required to start the app locally (everything has a dev default), but
you'll want at least these for real use:

```bash
export OPENAI_API_KEY=sk-...                  # AI trip planner
export JWT_SECRET=some-long-random-string-32-bytes-plus   # replace the dev default before deploying anywhere real
export ADMIN_EMAIL=admin@yourdomain.com        # optional, defaults shown below
export ADMIN_PASSWORD=SomeStrongerPassword!    # optional, defaults shown below

# Add these once you have accounts - all optional
export WEATHER_API_KEY=...            # OpenWeatherMap
export GOOGLE_MAPS_API_KEY=...        # Google Cloud Console, restrict by referrer
export CLOUDINARY_CLOUD_NAME=...
export CLOUDINARY_API_KEY=...
export CLOUDINARY_API_SECRET=...
export MAIL_ENABLED=true              # flips email from "log only" to actually sending
export MAIL_USERNAME=you@gmail.com
export MAIL_PASSWORD=your-app-password  # Gmail: use an App Password, not your real password
```

## 2. Run it

```bash
cd tourism-service
mvn spring-boot:run
```

Runs on `http://localhost:8081`.

An admin account is auto-seeded on first run:
- **email:** `admin@ampara-tourism.example` (or `ADMIN_EMAIL` if set)
- **password:** `ChangeMe123!` (or `ADMIN_PASSWORD` if set) — **change this before deploying anywhere public.**

## 3. Explore the API

- **Swagger UI:** `http://localhost:8081/swagger-ui.html` — click "Authorize" and paste a JWT (see below) to test protected endpoints interactively.
- **H2 console:** `http://localhost:8081/h2-console` — JDBC URL `jdbc:h2:mem:tourismdb`, user `sa`, no password.

### Auth flow

```bash
# Register a tourist
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Nimal","email":"nimal@example.com","password":"password123"}'

# Login (tourist or admin) - returns a JWT
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nimal@example.com","password":"password123"}'
```

Use the returned `token` as `Authorization: Bearer <token>` on protected calls.

### Booking a hotel / room (as a logged-in tourist)

```bash
curl -X POST http://localhost:8081/api/bookings \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"hotelId":1,"roomId":1,"checkIn":"2026-09-01","checkOut":"2026-09-03","guests":2}'
```

### Town dashboard

Open `/town.html?town=Pottuvil` for the unified town experience. It provides tourist places, hotels, room selection, hotel/room booking, Google Maps navigation, food cards and food ordering. Tourist registration/login is available at `/auth.html`.

Food ordering is available through `POST /api/food-orders` and is intentionally public for the demo; replace it with authenticated/customer-order workflow and payment integration before production.

### Admin dashboard

```bash
curl http://localhost:8081/api/admin/stats -H "Authorization: Bearer <admin-token>"
curl http://localhost:8081/api/admin/bookings -H "Authorization: Bearer <admin-token>"
curl http://localhost:8081/api/admin/users -H "Authorization: Bearer <admin-token>"
```

### Places, reviews & favourites

```bash
# Browse / filter / search places (all public, no login needed)
curl http://localhost:8081/api/places
curl "http://localhost:8081/api/places?town=Arugam%20Bay"
curl "http://localhost:8081/api/places?category=Surfing"
curl "http://localhost:8081/api/places/search?name=Oluvil"
curl "http://localhost:8081/api/places/nearby?lat=6.84&lng=81.83&limit=5"

# Leave a review (logged-in tourist)
curl -X POST http://localhost:8081/api/reviews \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"placeId":1,"rating":5,"comment":"Beautiful sunset spot!"}'

# Save a favourite
curl -X POST http://localhost:8081/api/favorites/1 -H "Authorization: Bearer <token>"
curl http://localhost:8081/api/favorites -H "Authorization: Bearer <token>"
```

## 4. Run the tests

```bash
mvn test
```

## 5. Run with Docker (PostgreSQL included)

```bash
docker compose up --build
```

This starts PostgreSQL plus the app on `http://localhost:8081`, with the app
talking to Postgres instead of H2 (via the `prod` Spring profile). Set
`JWT_SECRET`, `ADMIN_PASSWORD`, etc. as environment variables before running,
or edit the defaults in `docker-compose.yml` — don't ship the dev defaults to
a public deployment.

## 6. Deploy to Render

`render.yaml` defines a web service (built from the `Dockerfile`) plus a managed
PostgreSQL database. In the Render dashboard: **New → Blueprint**, point it at
your GitHub repo, and Render provisions both from this file. Add your optional
API keys (OpenAI, Weather, Google Maps, Cloudinary) as environment variables in
the Render dashboard afterwards — they're left unset (`sync: false`) in the
blueprint since they're secrets.

## 7. CI/CD

`.github/workflows/ci.yml` runs `mvn clean verify` on every push/PR to `main`.
Push this project to GitHub as-is and it'll pick the workflow up automatically —
no secrets are required for the build to pass since all external keys default
to empty/placeholder values.

## Access rules summary

| Endpoint | Who |
|---|---|
| `POST /api/auth/**` | Public |
| `GET /api/attractions/**`, `/api/hotels/**`, `/api/places/**`, `/api/reviews/**` | Public |
| `GET /map.html` | Public |
| `GET /api/emergency-contacts/**`, `/api/transport/**`, `/api/nearby-facilities/**`, `/api/events/**`, `/api/gallery/**` | Public |
| `/api/i18n/**`, `/api/voice-guide/**`, `/api/offline/**` | Public |
| `POST /api/analytics/view` | Public |
| `GET /api/analytics/dashboard`, `GET /api/analytics/place/**` | Admin only |
| `POST/PUT/DELETE /api/attractions/**`, `/api/hotels/**`, `/api/places/**` | Admin only |
| `POST/PUT/DELETE /api/emergency-contacts/**`, `/api/transport/**`, `/api/nearby-facilities/**`, `/api/events/**`, `/api/gallery/**` | Admin only |
| `/api/admin/**` | Admin only |
| `/api/ai/**`, `/api/weather/**`, `/api/config/**` (GET) | Public |
| `/api/bookings/**`, `/api/images/**`, `POST /api/reviews`, `/api/favorites/**` | Any logged-in user |

## Notes / what's still a stub

- **Admin dashboard** is backend-only (REST endpoints), per your original request
  — no dedicated admin frontend. Point any admin UI at `/api/admin/*`.
- **Weather/Maps-key/Cloudinary/Email** work as soon as you add real credentials
  — no code changes needed, just set the env vars above.
- **Local dev persistence**: H2 in-memory by default (data resets on restart) —
  use `docker compose up` or the `prod` profile for persistent PostgreSQL.
- I could not run `mvn clean verify` in this sandbox (no Maven Central network
  access here), so this was a careful manual review, not a compiled build —
  please run `mvn clean verify` yourself as the real check before deploying.
- See the "seeded places data" section above — most place-level details are
  placeholders you should verify and fill in, not researched facts.

## தமிழில் சுருக்கமான வழிகாட்டி (Quick Tamil summary)

**இயக்குவது எப்படி (Local run):**
```bash
cd tourism-service
mvn spring-boot:run
```
பிறகு `http://localhost:8081/map.html` திறந்தால் Interactive Map தெரியும்.
Swagger: `http://localhost:8081/swagger-ui.html`.

**Hosting (இலவசமாக):**
1. இந்த Project-ஐ GitHub-க்கு push செய்யவும்.
2. [Render.com](https://render.com) - New → Blueprint → உங்கள் repo-ஐ தேர்ந்தெடுக்கவும்.
   `render.yaml` file தானாக Web Service + PostgreSQL database-ஐ உருவாக்கும்.
3. Deploy ஆனதும், Environment tab-இல் உங்கள் optional API keys (OpenAI, Weather,
   Google Maps, Cloudinary) சேர்க்கவும்.
4. முடிந்தது — உங்கள் App public URL-இல் இயங்கும்.

**Local-ல் Docker வேண்டுமெனில்:**
```bash
docker compose up --build
```
இது PostgreSQL + App இரண்டையும் ஒரே கட்டளையில் இயக்கும்.

**முக்கியம்:** Seed செய்யப்பட்ட 131 இடங்களில் Arugam Bay surf points, Rajagala,
Magul Maha Viharaya போன்ற சில இடங்கள் மட்டுமே உறுதியான தகவல். மீதமுள்ளவை
Admin API வழியாக நீங்கள் சரிபார்த்து புதுப்பிக்க வேண்டிய Placeholder தரவு.


## Town-wise Tourism Guide (added)
The project now includes `/town.html?town=Pottuvil`, a town-centric view that separates:
- Tourist Places
- Hotels & Rooms
- Food & Local Dishes

New backend resources:
- `HotelRoom` entity + `/api/rooms`
- `FoodItem` entity + `/api/foods`
- Hotel town/latitude/longitude fields and town filtering
- Starter room/food records for the main Ampara tourism towns

The seeded hotel/food entries are starter/demo data and should be verified/replaced with real business information before production.

## Live-data AI Trip Planner

The town dashboard now includes `POST /api/ai/trip-plan`. The planner combines the town's
stored tourist places with live provider data when configured:

- Google Places API (New): place status, current opening hours, ratings and map links.
- Google Routes API: traffic-aware driving routes and transit routes/departure information when
  supported by the region/provider data.
- OpenWeatherMap: current weather or forecast data for a trip date.
- Stored transport routes: used only as a clearly-labelled scheduled fallback when live transit
  data is unavailable.

### Environment variables

```text
OPENAI_API_KEY=...
GOOGLE_MAPS_API_KEY=...
WEATHER_API_KEY=...
```

Optional provider URL overrides:

```text
GOOGLE_PLACES_URL=https://places.googleapis.com/v1/places:searchText
GOOGLE_ROUTES_URL=https://routes.googleapis.com/directions/v2:computeRoutes
WEATHER_FORECAST_URL=https://api.openweathermap.org/data/2.5/forecast
```

The Google Maps Platform APIs are pay-as-you-go. Keep field masks narrow, cache live results,
and monitor quotas/costs in Google Cloud. Live transit results are provider/coverage dependent;
the application never labels a stored timetable as live.


## Bus timetable sources
The transport dashboard aggregates published schedules from BusTimetable.lk and TimeKeeper.lk. Results are labelled scheduled, not live GPS data. See `BUS_TIMETABLE_INTEGRATION.md`.


### Production environment variables
For Render, set `SPRING_PROFILES_ACTIVE=prod` and configure `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `APP_CORS_ALLOWED_ORIGINS`, and the optional external-service secrets.
