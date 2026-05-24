package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
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
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.RunwayDisplayStatus
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.domain.calculation.WindCalculationEngine

@Composable
fun WindWeatherCard(
    weather: WidgetWeatherData,
    runwayDashboard: RunwayDashboardState,
    modifier: Modifier = Modifier
) {
    val components = runwayDashboard.components
    val hasWind = weather.windSpeedKt > 0 || weather.windDirection > 0
    AviationInstrumentCard(style = InstrumentStyle.WEATHER_WIND, modifier = modifier, contentPadding = 8.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Air, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.85f))
            Text("WIND", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (hasWind) "${weather.windDirection}° @ ${weather.windSpeedKt} kt" else "Loading…",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                if (runwayDashboard.status == RunwayDisplayStatus.READY) {
                    val hw = if (components.headwindKt >= components.tailwindKt) {
                        WindCalculationEngine.formatHeadwind(components.headwindKt)
                    } else {
                        WindCalculationEngine.formatTailwind(components.tailwindKt)
                    }
                    WeatherLine("Headwind", hw)
                    WeatherLine("Crosswind", "%.0f kt".format(kotlin.math.abs(components.crosswindKt)))
                    WeatherLine("Tailwind", "%.0f kt".format(components.tailwindKt))
                }
            }
            WindRoseCanvas(
                windFromDegrees = weather.windDirection,
                size = 72.dp,
                showFromLabel = true
            )
        }
    }
}
