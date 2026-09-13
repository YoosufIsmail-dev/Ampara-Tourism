
/* Sri Lanka Department of Meteorology integration */
const METEO_SOURCE_URL = "https://meteo.gov.lk/";
const WEATHER_API_BASE = "/api/weather";

async function loadMeteoWeather(town) {
  const container = document.querySelector("[data-meteo-weather]");
  if (!container || !town) return;
  container.innerHTML = '<div class="weather-loading">Loading official Sri Lanka weather data…</div>';

  try {
    const response = await fetch(`${WEATHER_API_BASE}?town=${encodeURIComponent(town)}`, {
      headers: { "Accept": "application/json" }
    });
    if (!response.ok) throw new Error("Weather service unavailable");
    const data = await response.json();

    container.innerHTML = `
      <div class="weather-card__top">
        <div>
          <span class="weather-card__eyebrow">Official Weather</span>
          <h3>${escapeHtml(data.location || town)}</h3>
        </div>
        <span class="weather-status weather-status--official">METEO.GOV.LK</span>
      </div>
      <div class="weather-card__metrics">
        <div><strong>${escapeHtml(data.temperature ?? "—")}°C</strong><span>Temperature</span></div>
        <div><strong>${escapeHtml(data.condition ?? "—")}</strong><span>Condition</span></div>
        <div><strong>${escapeHtml(data.rainfall ?? "—")}</strong><span>Rainfall</span></div>
        <div><strong>${escapeHtml(data.wind ?? "—")}</strong><span>Wind</span></div>
      </div>
      <div class="weather-card__footer">
        <span>Updated: ${escapeHtml(data.updatedAt ?? "—")}</span>
        <a href="${METEO_SOURCE_URL}" target="_blank" rel="noopener">Official source ↗</a>
      </div>
    `;
  } catch (error) {
    container.innerHTML = `
      <div class="weather-error">
        <strong>Official weather data unavailable right now.</strong>
        <p>Please check the Sri Lanka Department of Meteorology directly.</p>
        <a href="${METEO_SOURCE_URL}" target="_blank" rel="noopener">Open meteo.gov.lk ↗</a>
      </div>`;
  }
}

function escapeHtml(value) {
  return String(value ?? "").replace(/[&<>"']/g, ch => ({
    "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"
  }[ch]));
}

document.addEventListener("DOMContentLoaded", () => {
  const town = new URLSearchParams(location.search).get("town");
  if (town) loadMeteoWeather(town);
});
