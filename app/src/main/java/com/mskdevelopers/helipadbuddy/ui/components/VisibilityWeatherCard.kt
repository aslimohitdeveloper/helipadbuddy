package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData

@Composable
fun VisibilityWeatherCard(
    weather: WidgetWeatherData,
    modifier: Modifier = Modifier
) {
    val flightCategory = flightCategoryFor(weather.visibilityKm)
    AviationInstrumentCard(style = InstrumentStyle.WEATHER_CLOUD, modifier = modifier, contentPadding = 8.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.85f))
            Text("VISIBILITY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.padding(start = 4.dp))
        }
        Column {
            WeatherLine("Visibility", if (weather.visibilityKm > 0f) "%.0f km".format(weather.visibilityKm) else "—")
            WeatherLine("Category", flightCategory)
            WeatherLine("Station", weather.station.ifBlank { "GPS" })
            WeatherLine("Source", weather.weatherSource.ifBlank { "—" })
        }
    }
}

private fun flightCategoryFor(visibilityKm: Float): String = when {
    visibilityKm <= 0f -> "—"
    visibilityKm >= 8f -> "VFR"
    visibilityKm >= 5f -> "MVFR"
    visibilityKm >= 1.6f -> "IFR"
    else -> "LIFR"
}
