
/**
 * Weather-aware AI Trip Planner
 * Weather data must come from the backend's verified/official weather source.
 * The planner adjusts activity recommendations based on conditions.
 */
function weatherAwareRecommendations(places, weather = {}) {
  const condition = String(weather.condition || "").toLowerCase();
  const rainChance = Number(weather.rainProbability ?? weather.rainChance ?? -1);
  const warning = String(weather.warning || "").toLowerCase();

  const severe = /storm|thunder|lightning|cyclone|severe|flood|strong wind/.test(
    `${condition} ${warning}`
  );
  const rainy = rainChance >= 60 || /rain|shower|thunder/.test(condition);
  const hot = /hot|very warm/.test(condition) || Number(weather.temperature) >= 33;

  return (places || []).map(place => {
    const type = String(place.tourismType || place.category || "").toUpperCase();
    const indoor = /CULTURAL|HISTORICAL|RELIGIOUS|MUSEUM|INDOOR/.test(type);
    const outdoor = /NATURAL|BEACH|ADVENTURE|WILDLIFE|SURF|OUTDOOR/.test(type);

    let score = Number(place.score || place.rating || 0);
    let reason = "Good fit for the current conditions.";

    if (severe && outdoor) {
      score -= 100;
      reason = "Outdoor activity reduced because of a weather warning.";
    } else if (rainy && outdoor) {
      score -= 35;
      reason = "Outdoor activity reduced because rain is expected.";
    } else if (rainy && indoor) {
      score += 30;
      reason = "Indoor/cultural activity preferred because rain is expected.";
    } else if (hot && outdoor) {
      score -= 10;
      reason = "Consider an early morning or late afternoon visit.";
    }

    return { ...place, weatherScore: score, weatherReason: reason };
  }).sort((a, b) => b.weatherScore - a.weatherScore);
}

function buildWeatherAwareDayPlan({ places, weather, startHour = 8, endHour = 20 }) {
  const ranked = weatherAwareRecommendations(places, weather);
  const severe = /storm|thunder|lightning|cyclone|severe|flood|strong wind/i.test(
    `${weather?.condition || ""} ${weather?.warning || ""}`
  );

  const slots = [];
  const preferred = ranked.filter(p => p.weatherScore > -20);
  const fallback = ranked.filter(p => p.weatherScore <= -20);

  if (severe) {
    slots.push({
      time: `${String(startHour).padStart(2, "0")}:00`,
      activity: "Weather safety check",
      note: "Review the latest official weather advisory before travelling."
    });
  }

  const chosen = (preferred.length ? preferred : fallback).slice(0, 4);
  chosen.forEach((place, index) => {
    const hour = Math.min(startHour + index * 3, endHour - 1);
    slots.push({
      time: `${String(hour).padStart(2, "0")}:00`,
      activity: place.name,
      type: place.tourismType || place.category,
      note: place.weatherReason
    });
  });

  return {
    generatedAt: new Date().toISOString(),
    weatherSource: weather?.source || "Official weather source",
    weatherStatus: weather?.status || "VERIFY",
    slots
  };
}
