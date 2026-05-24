package com.mskdevelopers.helipadbuddy.data.model

import com.mskdevelopers.helipadbuddy.data.model.TrendDirection
import kotlinx.serialization.Serializable

@Serializable
data class WidgetWeatherData(
    val station: String = "",
    val windDirection: Int = 0,
    val windSpeedKt: Int = 0,
    val temperature: Float = 0f,
    val dewPoint: Float = 0f,
    val humidity: Int = 0,
    val visibilityKm: Float = 0f,
    val cloudCover: Int = 0,
    val weather: String = "",
    val qnh: Float = 1013.25f,
    val qnhMetarHpa: Float = 0f,
    val qfeHpa: Float = 0f,
    val refreshSpinFrame: Int = 0,
    val qnhPhoneHpa: Float = 0f,
    val qffHpa: Float = 0f,
    val pressureTrend: Float = 0f,
    val pressureTrendDirection: String = TrendDirection.STABLE.name,
    val metarRaw: String = "",
    val weatherSource: String = "Open-Meteo (GPS)",
    val isRefreshing: Boolean = false,
    val altitudeMsl: Int = 0,
    val satelliteCount: Int = 0,
    val gpsQuality: String = "NO_FIX",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val activeRunwayEnd: String = "",
    val headwindKt: Float = 0f,
    val tailwindKt: Float = 0f,
    val crosswindKt: Float = 0f,
    val crosswindSide: String = "",
    val runwayConfigured: Boolean = false,
    val alertSeverity: String = "",
    val updatedAtMillis: Long = 0L,
    val cloudLow: String = "",
    val cloudMedium: String = "",
    val cloudHigh: String = "",
    val ceilingFt: Int = 0,
    val elevationMslMeters: Int = 0,
    val forecastPoints: List<ForecastPoint> = emptyList(),
    val openMeteoDiagnostics: String = ""
) {
    companion object {
        val EMPTY = WidgetWeatherData()
    }
}

@Serializable
data class MetarStation(
    val icao: String,
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class TerrainData(
    val terrainElevationMeters: Double = 0.0,
    val terrainElevationFeet: Double = 0.0,
    val aglMeters: Double = 0.0,
    val aglFeet: Double = 0.0,
    val warningLevel: TerrainWarning = TerrainWarning.NONE,
    val isAvailable: Boolean = false,
    val terrainUnavailableMessage: String = "Terrain unavailable"
) {
    enum class TerrainWarning { NONE, LOW_CLEARANCE, RAPID_CLOSURE }
    companion object { val EMPTY = TerrainData() }
}

@Serializable
data class WindData(
    val directionDeg: Int = 0,
    val speedKts: Float = 0f,
    val crosswindKts: Float = 0f,
    val headwindKts: Float = 0f
) {
    val crosswindSide: String get() = when {
        crosswindKts > 0.5f -> "Right"
        crosswindKts < -0.5f -> "Left"
        else -> "None"
    }
    companion object { val EMPTY = WindData() }
}
