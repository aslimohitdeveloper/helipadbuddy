package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.TerrainData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import kotlin.math.abs

private val QualityGood = Color(0xFF4CAF50)
private val QualityMarginal = Color(0xFFFFB300)
private val QualityPoor = Color(0xFFE53935)

@Composable
fun AltitudeCard(
    position: PositionData,
    pressure: PressureData,
    terrain: TerrainData = TerrainData.EMPTY,
    altitudeFeet: Boolean = true,
    modifier: Modifier = Modifier
) {
    val displayMsl = if (altitudeFeet) position.altitudeMslFeet.toInt() else position.altitudeMslMeters.toInt()
    val displayWgs84 = if (altitudeFeet) position.altitudeWgs84Feet.toInt() else position.altitudeWgs84Meters.toInt()
    val unitLabel = if (altitudeFeet) "ft" else "m"

    val animatedMsl by animateIntAsState(
        targetValue = displayMsl,
        animationSpec = tween(400),
        label = "mslAlt"
    )

    val qualityLabel = when (position.fixQuality) {
        "GOOD" -> "GOOD"
        "MARGINAL" -> "MARGINAL"
        else -> "POOR"
    }
    val qualityColor = when (qualityLabel) {
        "GOOD" -> QualityGood
        "MARGINAL" -> QualityMarginal
        else -> QualityPoor
    }

    val trendFtMin = remember(pressure.altitudeTrend10sFtMin) { pressure.altitudeTrend10sFtMin }

    AviationInstrumentCard(style = InstrumentStyle.ALTITUDE, modifier = modifier, contentPadding = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "GPS ALTITUDE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = qualityColor.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .width(90.dp)
                            .height(30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            qualityLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = qualityColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = animatedMsl,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "mslAnim"
        ) { value ->
            ResponsiveValueText(
                text = value.toString(),
                color = SignalBlue,
                digitCountOverride = value.toString().length
            )
        }
        Text(
            text = unitLabel,
            style = MaterialTheme.typography.titleSmall,
            color = Color.White.copy(alpha = 0.85f),
            maxLines = 1
        )

        if (abs(trendFtMin) >= 1f) {
            Text(
                text = "Trend: ${if (trendFtMin >= 0) "+" else ""}${"%.0f".format(trendFtMin)} $unitLabel/min",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }

        Text(
            if (altitudeFeet) "Pressure Alt: %.0f ft".format(pressure.pressureAltitudeFeet)
            else "Pressure Alt: %.0f m".format(pressure.pressureAltitudeMeters),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            if (altitudeFeet) "Density Alt: %.0f ft".format(pressure.densityAltitudeFeet)
            else "Density Alt: %.0f m".format(pressure.densityAltitudeMeters),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            "WGS84: $displayWgs84 $unitLabel",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.55f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        if (!terrain.isAvailable) {
            Text(
                terrain.terrainUnavailableMessage,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}
