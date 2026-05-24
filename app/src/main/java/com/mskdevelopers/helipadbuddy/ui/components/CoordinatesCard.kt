package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.data.model.TerrainData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.WeatherRepository

@Composable
fun CoordinatesCard(
    position: PositionData,
    weather: WidgetWeatherData,
    terrain: TerrainData,
    nearestStationHint: String? = null,
    nearestDistanceKm: Double? = null,
    modifier: Modifier = Modifier
) {
    val elevM = when {
        weather.elevationMslMeters > 0 -> weather.elevationMslMeters
        terrain.isAvailable -> terrain.terrainElevationMeters.toInt()
        else -> position.altitudeMslMeters.toInt()
    }
    val sourceLabel = when (weather.weatherSource) {
        WeatherRepository.WEATHER_SOURCE_METAR_CLOUDS,
        WeatherRepository.WEATHER_SOURCE_METAR_ENHANCED -> "Open-Meteo + METAR clouds"
        else -> weather.weatherSource.ifBlank { "Open-Meteo (GPS)" }
    }
    val stationLine = when {
        weather.station.isNotBlank() && weather.station != "GPS" -> weather.station
        !nearestStationHint.isNullOrBlank() -> "Nearest: $nearestStationHint"
        else -> "Auto station"
    }
    val distanceLine = nearestDistanceKm?.let { "%.0f km to nearest".format(it) }

    AviationInstrumentCard(style = InstrumentStyle.COORDINATES, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("CURRENT LOCATION", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                WeatherLine("Lat", "%.5f".format(position.latitude))
                WeatherLine("Lon", "%.5f".format(position.longitude))
                WeatherLine("Elev MSL", "$elevM m")
            }
            Column(modifier = Modifier.weight(1f)) {
                WeatherLine("Station", stationLine)
                if (distanceLine != null) {
                    WeatherLine("Distance", distanceLine)
                }
                WeatherLine("Source", sourceLabel)
                if (weather.updatedAtMillis > 0L) {
                    val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date(weather.updatedAtMillis))
                    WeatherLine("Updated", time)
                }
            }
        }
    }
}
