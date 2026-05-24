package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.remote.AviationMetarResponse
import com.mskdevelopers.helipadbuddy.data.remote.OpenMeteoCurrent
import com.mskdevelopers.helipadbuddy.domain.calculation.WeatherDerivedValues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Primary weather fetcher: Open-Meteo coordinate weather first, METAR optional enhancement.
 */
class WeatherRepository(context: Context) {

    private val nearestAirport = NearestAirportRepository(context)
    private val metarRepo = MetarRepository()
    private val openMeteoRepo = WeatherWidgetRepository()

    suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        altitudeMsl: Int,
        sats: Int,
        gpsQuality: String,
        pressure: PressureWidgetData? = null,
        preferredIcao: String? = null
    ): WidgetWeatherData = withContext(Dispatchers.IO) {
        val openMeteo = openMeteoRepo.fetchForecast(latitude, longitude)
        val manualIcao = preferredIcao
            ?.trim()
            ?.uppercase()
            ?.takeIf { it.length == 4 && it.all { ch -> ch.isLetter() } }
        val manualStation = manualIcao?.let { nearestAirport.findByIcao(it) }
        val autoStation = if (manualIcao == null) nearestAirport.findNearest(latitude, longitude) else null
        val icao = manualIcao ?: autoStation?.icao.orEmpty()
        val metar = when {
            manualIcao != null && manualStation == null -> null
            icao.isNotEmpty() -> metarRepo.fetchMetar(icao)
            else -> null
        }
        val elevationMeters = openMeteoRepo.fetchElevationMeters(latitude, longitude)?.toInt() ?: altitudeMsl
        val forecastPoints = openMeteoRepo.fetchHourlyForecast(latitude, longitude)
        val openMeteoDiagnostics = formatOpenMeteoDiagnostics(latitude, longitude, openMeteo, forecastPoints)

        val base = buildFromOpenMeteo(
            openMeteo = openMeteo,
            latitude = latitude,
            longitude = longitude,
            altitudeMsl = altitudeMsl,
            sats = sats,
            gpsQuality = gpsQuality,
            station = icao,
            elevationMslMeters = elevationMeters,
            forecastPoints = forecastPoints,
            openMeteoDiagnostics = openMeteoDiagnostics
        )
        val enhanced = mergeMetarEnhancements(base, metar, icao)
        if (pressure != null && pressure.qfeHpa > 0f) {
            openMeteoRepo.mergePressureIntoWidget(enhanced, pressure)
        } else {
            enhanced
        }
    }

    private fun buildFromOpenMeteo(
        openMeteo: OpenMeteoCurrent?,
        latitude: Double,
        longitude: Double,
        altitudeMsl: Int,
        sats: Int,
        gpsQuality: String,
        station: String,
        elevationMslMeters: Int,
        forecastPoints: List<com.mskdevelopers.helipadbuddy.data.model.ForecastPoint>,
        openMeteoDiagnostics: String
    ): WidgetWeatherData {
        val rawTemp = openMeteo?.temperature2m?.toFloat() ?: 0f
        val rawDew = openMeteo?.dewpoint2m?.toFloat()
            ?: WeatherDerivedValues.dewPointFromRelativeHumidity(
                rawTemp,
                openMeteo?.relativeHumidity2m ?: 0
            )
        val rawRh = openMeteo?.relativeHumidity2m ?: 0
        val thermo = WeatherDerivedValues.normalizeThermodynamics(rawTemp, rawDew, rawRh)

        val windDir = openMeteo?.windDirection10m ?: 0
        val windSpd = openMeteo?.windSpeed10m?.toInt() ?: 0
        val vis = openMeteo?.visibility?.div(1000f)?.toFloat() ?: 0f
        val cloudCover = openMeteo?.cloudCover ?: 0
        val weather = openMeteoRepo.weatherCodeDescription(openMeteo?.weatherCode)
        val modelQnh = openMeteo?.pressureMsl?.toFloat()?.takeIf { it > 0f } ?: 1013.25f

        return WidgetWeatherData(
            station = station.ifEmpty { "GPS" },
            windDirection = windDir,
            windSpeedKt = windSpd,
            temperature = thermo.temperatureC,
            dewPoint = thermo.dewPointC,
            humidity = thermo.humidityPercent,
            visibilityKm = vis,
            cloudCover = cloudCover,
            weather = weather,
            qnh = modelQnh,
            metarRaw = "",
            weatherSource = WEATHER_SOURCE_COORDINATE,
            altitudeMsl = altitudeMsl,
            satelliteCount = sats,
            gpsQuality = gpsQuality,
            latitude = latitude,
            longitude = longitude,
            elevationMslMeters = elevationMslMeters,
            forecastPoints = forecastPoints,
            openMeteoDiagnostics = openMeteoDiagnostics,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    private fun mergeMetarEnhancements(
        base: WidgetWeatherData,
        metar: AviationMetarResponse?,
        icao: String
    ): WidgetWeatherData {
        if (metar == null) return base
        val qnhMetar = metarRepo.parseMetarQnhHpa(metar.altim).takeIf { it > 0f }
        val layers = metarRepo.parseCloudLayers(metar.clouds)
        return base.copy(
            station = icao.ifEmpty { base.station },
            metarRaw = metar.rawOb.orEmpty(),
            weatherSource = WEATHER_SOURCE_METAR_CLOUDS,
            qnhMetarHpa = qnhMetar ?: 0f,
            cloudCover = base.cloudCover,
            cloudLow = layers.low,
            cloudMedium = layers.medium,
            cloudHigh = layers.high,
            ceilingFt = layers.ceilingFt
        )
    }

    private fun formatOpenMeteoDiagnostics(
        latitude: Double,
        longitude: Double,
        openMeteo: OpenMeteoCurrent?,
        forecastPoints: List<com.mskdevelopers.helipadbuddy.data.model.ForecastPoint>
    ): String = buildString {
        appendLine("Open-Meteo @ %.5f, %.5f".format(latitude, longitude))
        if (openMeteo == null) {
            appendLine("Current: unavailable")
        } else {
            appendLine("Temp: ${openMeteo.temperature2m}°C")
            appendLine("Dew: ${openMeteo.dewpoint2m}°C")
            appendLine("RH: ${openMeteo.relativeHumidity2m}%")
            val t = openMeteo.temperature2m?.toFloat() ?: 0f
            val rh = openMeteo.relativeHumidity2m ?: 0
            if (t != 0f) {
                appendLine("Vapor e: ${"%.1f".format(WeatherDerivedValues.vaporPressureHpa(t, rh))} hPa")
                appendLine("Vapor eₛ: ${"%.1f".format(WeatherDerivedValues.saturationVaporPressureHpa(t))} hPa")
            }
            appendLine("Wind: ${openMeteo.windDirection10m}° @ ${openMeteo.windSpeed10m} kt")
            appendLine("Vis: ${openMeteo.visibility?.div(1000f)} km")
            appendLine("Cloud: ${openMeteo.cloudCover}%")
            appendLine("Pressure MSL: ${openMeteo.pressureMsl} hPa")
            appendLine("Code: ${openMeteo.weatherCode} → ${openMeteoRepo.weatherCodeDescription(openMeteo.weatherCode)}")
        }
        appendLine("Forecast slots: ${forecastPoints.size}")
        forecastPoints.forEach { p ->
            appendLine("  ${p.timeLabel}: ${p.temperature}°C ${p.windDirection}°@${p.windSpeedKt}kt precip ${p.precipProbability}%")
        }
    }.trim()

    companion object {
        const val WEATHER_SOURCE_COORDINATE = "Open-Meteo (GPS)"
        const val WEATHER_SOURCE_METAR_CLOUDS = "Open-Meteo + METAR clouds"
        @Deprecated("Use WEATHER_SOURCE_METAR_CLOUDS", ReplaceWith("WEATHER_SOURCE_METAR_CLOUDS"))
        const val WEATHER_SOURCE_METAR_ENHANCED = WEATHER_SOURCE_METAR_CLOUDS
    }
}
