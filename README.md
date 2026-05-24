# Helipad Buddy v2.0

Turn your smartphone into a smart aviation console. Helipad Buddy combines GNSS, MSL altitude correction, Open-Meteo weather, barometer fusion, optional METAR enrichment, runway calculations, sensor fusion, and home screen widgets.

## Highlights in v2.0

- **MSL altitude correction** — EGM96 geoid and terrain-aware MSL display
- **Open-Meteo weather engine** — GPS-coordinate wind, visibility, temperature, and pressure MSL (primary source)
- **METAR enrichment** — Cloud layers, ceiling, and raw METAR when a station is nearby (does not override GPS wind/vis)
- **QFE / QNH / QFF** — Barometer fusion with model QNH and optional METAR QNH reference
- **Runway intelligence** — Headwind, tailwind, crosswind, and active runway selection
- **Home screen widget** — Altitude, wind, runway components, pressure, refresh (standard and compact sizes)
- **METAR weather dashboard** — Wind rose, forecast, vapor pressure (e / eₛ), aligned tile layout
- **Flight logging** — Session recording, replay, and analytics
- **Sensor health** — Diagnostics for GNSS, barometer, motion, and fusion quality
- **Alerts** — Configurable thresholds for crosswind, pressure, and related conditions

## Core capabilities (v1 + v2)

- Precision positioning (lat/lon, altitude, ground speed)
- Aviation pressure system (QFE, QNH, density altitude)
- GNSS satellite monitoring
- Vertical speed indicator (barometric fusion)
- Track vs heading
- Intelligent night mode

## Weather sources

| Source | Used for |
|--------|----------|
| Open-Meteo (GPS) | Wind, visibility, temperature, pressure MSL, forecast |
| METAR (nearest/manual ICAO) | Cloud layers, ceiling, raw text, reference QNH |

Labels in-app: `Open-Meteo (GPS)` or `Open-Meteo + METAR clouds`.

## Build

Requires Android Studio with JDK 17+.

```bash
./gradlew assembleDebug
```

Install: `app/build/outputs/apk/debug/app-debug.apk`

## Website & screenshots

Marketing page: `helipadbuddy.php` (PHP + Tailwind/DaisyUI, deploy alongside your site `header.php` / `footer.php`).

Screenshot placeholders: add `assets/dashboard.jpg`, `assets/widget.jpg`, `assets/weather.jpg`, `assets/sensor.jpg` (see [assets/README.md](assets/README.md)).

## Known limitations

- GPS altitude varies by device; manual helipad elevation improves pressure accuracy
- Widget refresh may be delayed by Android battery optimization
- Weather APIs provide model estimates, not official briefings
- Some devices lack a barometer
- METAR may be unavailable in remote areas

**Situational awareness only — not certified for flight-critical navigation.**

## Open source

Contributions welcome.

```bash
git clone https://github.com/aslimohitdeveloper/helipadbuddy.git
```

## License

See repository license terms.
