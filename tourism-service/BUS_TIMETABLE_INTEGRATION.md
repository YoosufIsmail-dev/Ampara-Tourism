# Bus timetable integrations

This project aggregates two public published timetable sources:

1. BusTimetable.lk — `https://bustimetable.lk/makumbura-highway-bus-time-table/`
2. TimeKeeper.lk — `https://www.timekeeper.lk/`

## API

`GET /api/transport/bus-live?origin=Kandy&destination=Colombo&town=Colombo`

The response contains:

- `routes`: de-duplicated published timetable rows from both sources
- `sources`: the raw source payloads
- `source`: `BusTimetable.lk + TimeKeeper.lk`
- `status`: `SCHEDULED` when matching published rows exist

## Important

These sources publish timetables. They are **not live GPS vehicle telemetry** and should never be presented as current vehicle location or guaranteed availability. TimeKeeper explicitly warns that departure/arrival times may not be 100% accurate and can be affected by weather and traffic.

The UI labels these results `Scheduled` and links each result back to its source.

## Configuration

- `BUS_TIMETABLE_BASE_URL` (default `https://bustimetable.lk`)
- `BUS_TIMETABLE_CACHE_SECONDS` (default `600`)
- `TIMEKEEPER_BASE_URL` (default `https://www.timekeeper.lk`)
- `TIMEKEEPER_CACHE_SECONDS` (default `600`)

## AI Trip Planner

The combined timetable payload is passed into the AI planner. The planner must label both sources as scheduled/not-live and must surface conflicts rather than silently selecting one.
