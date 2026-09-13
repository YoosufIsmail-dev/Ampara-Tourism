# Final Nearby Services Fixes

## Fixed in this update

1. **Food-item onclick XSS**
   - Removed dynamic food names from inline `onclick` attributes.
   - Added `data-action` / `data-food-id` attributes and delegated click handling.
   - Nearby food ordering uses the same safe mechanism.
   - Place and hotel names are no longer concatenated into inline JavaScript either.

2. **Room N+1 query**
   - Added `HotelRoomRepository.findByHotelIdIn(List<Long>)`.
   - Nearby hotel rooms are now loaded in one batch query and grouped in memory.

3. **Runtime food coordinate fallback**
   - Food items created later without coordinates no longer disappear silently.
   - If GPS is missing, the controller uses an average coordinate of GPS-enabled hotels in the same town as an approximate town-level fallback.
   - API returns `coordinateSource` (`item-gps` or `town-fallback`) and resolved coordinates.

4. **Nearby 'View All Rooms' UI bug**
   - Nearby hotel cards did not contain a `rooms-{id}` element, so the old toggle action could fail.
   - The new toggle creates an inline room panel inside the nearby card.

5. **Static verification**
   - All 7 inline JavaScript blocks in `town.html` pass `node --check`.
   - No dynamic `onclick` remains for nearby place, hotel booking, room booking, or food ordering.

## Build limitation

Maven is not installed in the sandbox, so a full `mvn clean verify` could not be executed here. The modified Java source was reviewed statically; dependency-resolved compilation should still be run in the project CI/local environment.
