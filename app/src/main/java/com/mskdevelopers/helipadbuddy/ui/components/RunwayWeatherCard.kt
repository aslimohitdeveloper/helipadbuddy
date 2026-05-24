package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.RunwayDisplayStatus
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.domain.calculation.WindCalculationEngine
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import com.mskdevelopers.helipadbuddy.ui.theme.SignalGreen
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RunwayWeatherCard(
    state: RunwayDashboardState,
    weather: WidgetWeatherData,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.RUNWAY, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Flight, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("RUNWAY", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 4.dp))
        }
        when (state.status) {
            RunwayDisplayStatus.NO_RUNWAY -> {
                Text("No runway selected", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
            }
            else -> {
                val runway = state.activeRunway
                val active = runway?.activeRunway.orEmpty()
                val h1 = runway?.heading1 ?: 0
                val h2 = runway?.heading2 ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.width(64.dp).fillMaxHeight()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val length = size.height * 0.75f
                        val halfW = size.width * 0.22f
                        drawLine(
                            color = Color.White.copy(alpha = 0.35f),
                            start = Offset(cx, cy - length / 2f),
                            end = Offset(cx, cy + length / 2f),
                            strokeWidth = halfW * 2f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = SignalGreen,
                            start = Offset(cx, cy - length / 2f),
                            end = Offset(cx, cy + length / 2f),
                            strokeWidth = 4f,
                            cap = StrokeCap.Round
                        )
                        val rad = Math.toRadians((runway?.activeHeadingDeg ?: 0).toDouble())
                        val arrowLen = length * 0.35f
                        drawLine(
                            color = SignalBlue,
                            start = Offset(cx, cy),
                            end = Offset(
                                cx + arrowLen * sin(rad).toFloat(),
                                cy - arrowLen * cos(rad).toFloat()
                            ),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "RUNWAY $active ACTIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = SignalGreen,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                WeatherLine("Orient", "${h1}° / ${h2}°")
                                if (state.status == RunwayDisplayStatus.READY) {
                                    WeatherLine(
                                        "Wind",
                                        "${state.windDirectionDeg}° @ ${"%.0f".format(state.windSpeedKts)} kt"
                                    )
                                } else {
                                    WeatherLine(
                                        "Wind",
                                        "${weather.windDirection}° @ ${weather.windSpeedKt} kt"
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                if (state.status == RunwayDisplayStatus.READY) {
                                    val c = state.components
                                    val hw = if (c.headwindKt >= c.tailwindKt) {
                                        WindCalculationEngine.formatHeadwind(c.headwindKt)
                                    } else {
                                        WindCalculationEngine.formatTailwind(c.tailwindKt)
                                    }
                                    WeatherLine("Headwind", hw)
                                    WeatherLine("Crosswind", formatXw(c))
                                    WeatherLine("Tailwind", "${"%.0f".format(c.tailwindKt)} kt")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatXw(components: com.mskdevelopers.helipadbuddy.data.model.RunwayWindData): String {
    val mag = kotlin.math.abs(components.crosswindKt)
    if (mag < 0.5f) return "0 kt"
    return "%.0f kt %s".format(mag, components.crosswindDirection).trim()
}
