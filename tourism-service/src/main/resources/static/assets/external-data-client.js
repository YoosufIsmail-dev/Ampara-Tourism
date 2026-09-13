
/* Frontend talks only to our Spring Boot integration endpoints. */
window.ExternalDataClient = {
  weather: (town) =>
    fetch(`/api/integrations/weather?town=${encodeURIComponent(town)}`).then(r => r.json()),

  busTimetable: (from, to) =>
    fetch(`/api/integrations/bus-timetable?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`).then(r => r.json()),

  timeKeeper: (from, to) =>
    fetch(`/api/integrations/timekeeper?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`).then(r => r.json()),

  directions: (origin, destination) =>
    fetch(`/api/integrations/directions?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`).then(r => r.json())
};
