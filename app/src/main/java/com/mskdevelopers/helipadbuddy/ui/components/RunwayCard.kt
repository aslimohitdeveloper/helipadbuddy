package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.RunwayDisplayStatus
import com.mskdevelopers.helipadbuddy.data.model.RunwayWindSeverity
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.domain.calculation.WindCalculationEngine
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import kotlin.math.abs

@Composable
fun RunwayCard(
    state: RunwayDashboardState,
    weather: WidgetWeatherData = WidgetWeatherData.EMPTY,
    pressure: PressureData = PressureData.EMPTY,
    modifier: Modifier = Modifier
) {
    val severityColor by animateColorAsState(
        targetValue = severityColor(state.severity),
        label = "runway_severity"
    )
    val qnh = weather.qnhPhoneHpa.takeIf { it > 0f } ?: weather.qnh.takeIf { it > 0f }
        ?: pressure.qnhHpa
    val qfe = weather.qfeHpa.takeIf { it > 0f } ?: pressure.qfeHpa

    AviationInstrumentCard(style = InstrumentStyle.RUNWAY, modifier = modifier, contentPadding = 8.dp) {
        when (state.status) {
            RunwayDisplayStatus.NO_RUNWAY -> {
                Text("RUNWAY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                Text("No runway selected", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            RunwayDisplayStatus.NO_WIND, RunwayDisplayStatus.CONFIGURE_RUNWAY -> {
                val runway = state.activeRunway
                Text(
                    "RUNWAY ${runway?.activeRunway.orEmpty()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = severityColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Text(
                    if (state.status == RunwayDisplayStatus.NO_WIND) "Acquiring wind" else "Configure runway",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            RunwayDisplayStatus.READY -> {
                val runway = state.activeRunway ?: return@AviationInstrumentCard
                val components = state.components
                val headwindValue = if (components.headwindKt >= components.tailwindKt) {
                    WindCalculationEngine.formatHeadwind(components.headwindKt)
                } else {
                    WindCalculationEngine.formatTailwind(components.tailwindKt)
                }
                val crosswindValue = formatCrosswindDisplay(components)

                Text(
                    "RUNWAY ${runway.activeRunway} ACTIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = severityColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        RunwayLine("Wind", "${state.windDirectionDeg}° @ ${"%.0f".format(state.windSpeedKts)} kt")
                        RunwayLine(
                            if (components.headwindKt >= components.tailwindKt) "Headwind" else "Tailwind",
                            headwindValue
                        )
                        RunwayLine("Crosswind", crosswindValue)
                        RunwayLine("Weather", weather.weather.ifBlank { "—" })
                        RunwayLine("QNH", if (qnh > 0f) "%.0f".format(qnh) else "—")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        RunwayLine("Temperature", "%.0f°C".format(weather.temperature))
                        RunwayLine("Dew Point", "%.0f°C".format(weather.dewPoint))
                        RunwayLine("Humidity", "${weather.humidity}%")
                        RunwayLine("Visibility", "%.0f km".format(weather.visibilityKm))
                        RunwayLine("QFE", if (qfe > 0f) "%.0f".format(qfe) else "—")
                    }
                }
            }
        }
    }
}

@Composable
private fun RunwayLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = SignalBlue,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun formatCrosswindDisplay(components: com.mskdevelopers.helipadbuddy.data.model.RunwayWindData): String {
    val magnitude = abs(components.crosswindKt)
    if (magnitude < 0.5f) return "0 kt"
    val side = when (components.crosswindDirection) {
        "Right" -> "Right"
        "Left" -> "Left"
        else -> ""
    }
    return "%.0f kt %s".format(magnitude, side).trim()
}

private fun severityColor(severity: RunwayWindSeverity): Color = when (severity) {
    RunwayWindSeverity.FAVORABLE -> Color(0xFF4CAF50)
    RunwayWindSeverity.MODERATE_CROSSWIND -> Color(0xFFFFC107)
    RunwayWindSeverity.HIGH_CROSSWIND -> Color(0xFFE53935)
}
