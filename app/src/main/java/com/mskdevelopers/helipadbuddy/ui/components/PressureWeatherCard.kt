package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.WeatherRepository

@Composable
fun PressureWeatherCard(
    weather: WidgetWeatherData,
    pressure: PressureData,
    modifier: Modifier = Modifier
) {
    val modelQnh = weather.qnh.takeIf { it > 0f }
    val metarQnh = weather.qnhMetarHpa.takeIf { it > 0f }
    val phoneQnh = weather.qnhPhoneHpa.takeIf { it > 0f } ?: pressure.qnhHpa
    val qfe = weather.qfeHpa.takeIf { it > 0f } ?: pressure.qfeHpa
    val qff = weather.qffHpa.takeIf { it > 0f } ?: 0f
    val trend = when (weather.pressureTrendDirection) {
        "RISING" -> "↑"
        "FALLING" -> "↓"
        else -> "→"
    }
    AviationInstrumentCard(style = InstrumentStyle.WEATHER_PRESSURE, modifier = modifier, contentPadding = 8.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Speed, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("PRESSURE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        Column {
            WeatherLine("QFE", if (qfe > 0f) "%.0f".format(qfe) else "—")
            WeatherLine("Model QNH", if (modelQnh != null) "%.0f".format(modelQnh) else "—")
            if (metarQnh != null) {
                WeatherLine("METAR QNH", "%.0f".format(metarQnh))
            }
            WeatherLine("Phone QNH", if (phoneQnh > 0f) "%.1f".format(phoneQnh) else "—")
            WeatherLine("QFF", if (qff > 0f) "%.0f".format(qff) else "—")
            WeatherLine("Trend", "$trend ${"%.1f".format(weather.pressureTrend)}")
        }
    }
}
