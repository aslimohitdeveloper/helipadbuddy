package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.ForecastPoint
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData

@Composable
fun ForecastWeatherCard(
    weather: WidgetWeatherData,
    modifier: Modifier = Modifier
) {
    val points = weather.forecastPoints.take(5)
    AviationInstrumentCard(style = InstrumentStyle.WEATHER_FORECAST, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("FORECAST", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (points.isEmpty()) {
                Text(
                    "Forecast unavailable",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    points.forEach { point ->
                        ForecastRow(point = point)
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastRow(point: ForecastPoint) {
    val lineStyle = MaterialTheme.typography.labelSmall
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            point.timeLabel,
            style = lineStyle,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(44.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            "%.0f°C".format(point.temperature),
            style = lineStyle,
            color = Color.White,
            modifier = Modifier.width(40.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            "${point.windDirection}° @ ${point.windSpeedKt} kt",
            style = lineStyle,
            color = Color.White,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            "${point.precipProbability}%",
            style = lineStyle,
            color = Color.White.copy(alpha = 0.65f),
            modifier = Modifier.width(32.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
