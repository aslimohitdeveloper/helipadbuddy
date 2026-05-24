package com.mskdevelopers.helipadbuddy.data.repository

import com.mskdevelopers.helipadbuddy.data.model.ForecastPoint
import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.remote.ApiJson
import com.mskdevelopers.helipadbuddy.data.remote.HttpClientProvider
import com.mskdevelopers.helipadbuddy.data.remote.OpenMeteoCurrent
import com.mskdevelopers.helipadbuddy.data.remote.OpenMeteoElevationResponse
import com.mskdevelopers.helipadbuddy.data.remote.OpenMeteoHourly
import com.mskdevelopers.helipadbuddy.data.remote.OpenMeteoResponse

class WeatherWidgetRepository(private val http: HttpClientProvider = HttpClientProvider()) {

    fun fetchForecast(latitude: Double, longitude: Double): OpenMeteoCurrent? {
        val url = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=$latitude&longitude=$longitude" +
            "&current=temperature_2m,relative_humidity_2m,dewpoint_2m,wind_speed_10m,wind_direction_10m,cloud_cover,visibility,weather_code,pressure_msl" +
            "&wind_speed_unit=kn"
        val body = http.get(url) ?: return null
        return try {
            ApiJson.instance.decodeFromString<OpenMeteoResponse>(body).current
        } catch (_: Exception) {
            null
        }
    }

    fun fetchHourlyForecast(latitude: Double, longitude: Double): List<ForecastPoint> {
        val url = "https://api.open-meteo.com/v1/forecast?" +
            "latitude=$latitude&longitude=$longitude" +
            "&hourly=temperature_2m,wind_speed_10m,wind_direction_10m,precipitation_probability,cloud_cover" +
            "&forecast_hours=5&wind_speed_unit=kn"
        val body = http.get(url) ?: return emptyList()
        return try {
            val hourly = ApiJson.instance.decodeFromString<OpenMeteoResponse>(body).hourly
            buildForecastSlots(hourly)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildForecastSlots(hourly: OpenMeteoHourly?): List<ForecastPoint> {
        if (hourly == null) return emptyList()
        val temps = hourly.temperature2m.orEmpty()
        val winds = hourly.windSpeed10m.orEmpty()
        val dirs = hourly.windDirection10m.orEmpty()
        val precip = hourly.precipitationProbability.orEmpty()
        val clouds = hourly.cloudCover.orEmpty()
        if (temps.isEmpty()) return emptyList()

        fun sampleAtHourOffset(hourOffset: Float): ForecastPoint {
            val index = hourOffset.coerceIn(0f, (temps.size - 1).toFloat())
            val i0 = index.toInt().coerceIn(0, temps.lastIndex)
            val i1 = (i0 + 1).coerceAtMost(temps.lastIndex)
            val t = index - i0
            fun lerp(a: Double, b: Double) = (a + (b - a) * t).toFloat()
            fun lerpI(a: Int, b: Int) = (a + (b - a) * t).toInt()
            return ForecastPoint(
                timeLabel = "",
                temperature = lerp(temps[i0], temps[i1]),
                windDirection = lerpI(dirs.getOrElse(i0) { 0 }, dirs.getOrElse(i1) { 0 }),
                windSpeedKt = lerp(winds.getOrElse(i0) { 0.0 }, winds.getOrElse(i1) { 0.0 }).toInt(),
                precipProbability = lerpI(precip.getOrElse(i0) { 0 }, precip.getOrElse(i1) { 0 }),
                cloudCover = lerpI(clouds.getOrElse(i0) { 0 }, clouds.getOrElse(i1) { 0 })
            )
        }

        val labels = listOf("Now", "+30m", "+60m", "+90m", "+120m")
        val hourOffsets = listOf(0f, 0.5f, 1f, 1.5f, 2f)
        return labels.zip(hourOffsets) { label, offset ->
            sampleAtHourOffset(offset).copy(timeLabel = label)
        }
    }

    fun fetchElevationMeters(latitude: Double, longitude: Double): Double? {
        val url = "https://api.open-meteo.com/v1/elevation?latitude=$latitude&longitude=$longitude"
        val body = http.get(url) ?: return null
        return try {
            ApiJson.instance.decodeFromString<OpenMeteoElevationResponse>(body)
                .elevation?.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    fun weatherCodeDescription(code: Int?): String = when (code) {
        0 -> "Clear"
        1, 2, 3 -> "Partly Cloudy"
        45, 48 -> "Fog"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }

    /** Merge live barometer pressure into widget weather snapshot. */
    fun mergePressureIntoWidget(
        current: WidgetWeatherData,
        pressure: PressureWidgetData
    ): WidgetWeatherData = current.copy(
        qfeHpa = pressure.qfeHpa,
        qnhPhoneHpa = pressure.qnhHpa,
        qffHpa = pressure.qffHpa,
        pressureTrend = pressure.pressureTrend,
        pressureTrendDirection = pressure.trendDirection.name,
    )
}
