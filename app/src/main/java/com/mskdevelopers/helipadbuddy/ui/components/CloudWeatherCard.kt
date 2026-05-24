package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData

@Composable
fun CloudWeatherCard(
    weather: WidgetWeatherData,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.WEATHER_CLOUD, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Cloud, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("CLOUDS", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        Column {
            WeatherLine("Cover", "${weather.cloudCover}%")
            WeatherLine("Low", weather.cloudLow.ifBlank { "—" })
            WeatherLine("Med", weather.cloudMedium.ifBlank { "—" })
            WeatherLine("High", weather.cloudHigh.ifBlank { "—" })
            WeatherLine("Ceiling", if (weather.ceilingFt > 0) "${weather.ceilingFt} ft" else "—")
        }
    }
}
