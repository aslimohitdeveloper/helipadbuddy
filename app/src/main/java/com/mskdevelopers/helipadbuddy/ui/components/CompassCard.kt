package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.rotate as drawRotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import com.mskdevelopers.helipadbuddy.ui.theme.SignalGreen
import com.mskdevelopers.helipadbuddy.ui.theme.SignalOrange
import com.mskdevelopers.helipadbuddy.ui.theme.SignalRed
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visual compass: 360° dial, smooth animated transitions, direction labels (N, NNE, etc.).
 * Displays GNSS signal strength and magnetic field strength.
 */
@Composable
fun CompassCard(
    position: PositionData,
    gnss: GnssHealthData,
    magneticStrength: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val headingDeg = position.headingDegrees

    // GNSS signal strength color
    val signalColor = when (gnss.quality) {
        GnssHealthData.GnssQuality.GOOD -> SignalGreen
        GnssHealthData.GnssQuality.MARGINAL -> SignalOrange
        GnssHealthData.GnssQuality.POOR,
        GnssHealthData.GnssQuality.NO_FIX -> SignalRed
    }

    // Magnetic strength color (Earth's field typically 25-65 μT)
    val magColor = when {
        magneticStrength >= 25f && magneticStrength <= 65f -> SignalGreen // Good
        (magneticStrength >= 20f && magneticStrength < 25f) || 
        (magneticStrength > 65f && magneticStrength <= 80f) -> SignalOrange // Marginal
        magneticStrength < 20f || magneticStrength > 80f -> SignalRed // Poor/interference
        else -> SignalBlue // Unknown/default
    }

    // Animate heading with wrap-around handling
    val animatedHeading by animateFloatAsState(
        targetValue = headingDeg,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "heading"
    )

    val directionLabel = AviationFormulas.degreesToDirection(headingDeg)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = onSurfaceVariant
                )
                Text(
                    "HDG %.0f° %s".format(headingDeg, directionLabel),
                    style = MaterialTheme.typography.labelMedium,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            var boxSize by remember { mutableStateOf(IntSize.Zero) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(4.dp)
                    .onSizeChanged { boxSize = it }
            ) {
                val w = boxSize.width.toFloat()
                val h = boxSize.height.toFloat()
                val cx = w / 2f
                val cy = h / 2f
                val radiusPx = minOf(w, h) / 2f - 12f
                val labelRadiusPx = radiusPx - 24f

                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                // Rotate entire dial opposite to heading so current heading is always at top
                // Needle stays fixed pointing up (North)
                drawRotate(-animatedHeading, pivot = Offset(cx, cy)) {
                    // Outer circle with better stroke
                    drawCircle(
                        color = onSurfaceVariant.copy(alpha = 0.6f),
                        radius = radiusPx,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3f)
                    )

                    // Draw 360° dial with tick marks every 10 degrees
                    for (deg in 0 until 360 step 10) {
                        val rad = Math.toRadians(deg.toDouble())
                        val isCardinal = deg % 90 == 0
                        val isIntercardinal = deg % 45 == 0 && !isCardinal
                        val isMajor = deg % 30 == 0

                        val tickLength = when {
                            isCardinal -> 16f
                            isIntercardinal -> 12f
                            isMajor -> 10f
                            else -> 6f
                        }
                        val tickWidth = when {
                            isCardinal -> 3f
                            isIntercardinal -> 2.5f
                            isMajor -> 2f
                            else -> 1.5f
                        }

                        val outerX = cx + radiusPx * cos(rad).toFloat()
                        val outerY = cy - radiusPx * sin(rad).toFloat()
                        val innerR = radiusPx - tickLength
                        val innerX = cx + innerR * cos(rad).toFloat()
                        val innerY = cy - innerR * sin(rad).toFloat()

                        drawLine(
                            color = onSurfaceVariant.copy(alpha = if (isCardinal || isIntercardinal) 1f else 0.7f),
                            start = Offset(innerX, innerY),
                            end = Offset(outerX, outerY),
                            strokeWidth = tickWidth
                        )
                    }

                }

                // Center dot (outside rotation, always visible) - blue for primary navigation
                drawCircle(
                    color = SignalBlue,
                    radius = 4f,
                    center = Offset(cx, cy)
                )

                // Needle: fixed pointing up (North), doesn't rotate - blue for primary navigation
                val needleLength = radiusPx * 0.65f
                val needleWidth = 10f
                val tailLength = 20f
                val path = Path().apply {
                    // Arrow head (pointing up/N)
                    moveTo(cx, cy - needleLength)
                    lineTo(cx - needleWidth, cy - needleLength + 20f)
                    lineTo(cx - 4f, cy + tailLength)
                    lineTo(cx, cy + tailLength + 4f)
                    lineTo(cx + 4f, cy + tailLength)
                    lineTo(cx + needleWidth, cy - needleLength + 20f)
                    close()
                }
                drawPath(path, SignalBlue)
                }
                
                // Draw cardinal direction labels (N, E, S, W) as overlay Text composables
                val directions = listOf(0f to "N", 90f to "E", 180f to "S", 270f to "W")
                for ((deg, label) in directions) {
                    val rad = Math.toRadians((deg - animatedHeading).toDouble())
                    val labelX = cx + labelRadiusPx * cos(rad).toFloat()
                    val labelY = cy - labelRadiusPx * sin(rad).toFloat()
                    Text(
                        text = label,
                        color = onSurface,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset { IntOffset((labelX - cx).toInt(), (labelY - cy).toInt()) }
                    )
                }
            }
            Text(
                directionLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = SignalBlue, // Blue for primary heading
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Text(
                    "TRK %.0f°".format(position.trackDegrees),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
                Text(
                    "Δ %.0f°".format(position.trackVsHeadingDegrees),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant
                )
            }
            // Signal strength indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                // GNSS signal strength with visual bars
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "GNSS: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                    // Signal strength bars (1-5 bars based on SNR)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            val barHeight = when {
                                gnss.averageSnrDbHz >= 30f && index < 5 -> 12.dp
                                gnss.averageSnrDbHz >= 25f && index < 4 -> 10.dp
                                gnss.averageSnrDbHz >= 20f && index < 3 -> 8.dp
                                gnss.averageSnrDbHz >= 15f && index < 2 -> 6.dp
                                gnss.averageSnrDbHz > 0f && index < 1 -> 4.dp
                                else -> 0.dp
                            }
                            if (barHeight > 0.dp) {
                                Canvas(
                                    modifier = Modifier
                                        .size(3.dp, barHeight)
                                        .padding(end = 1.dp)
                                ) {
                                    drawRect(color = signalColor)
                                }
                            }
                        }
                        Text(
                            " %.0f".format(gnss.averageSnrDbHz),
                            style = MaterialTheme.typography.labelSmall,
                            color = signalColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
                // Magnetic strength
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MAG: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = onSurfaceVariant
                    )
                    Text(
                        "%.0f μT".format(magneticStrength),
                        style = MaterialTheme.typography.labelSmall,
                        color = magColor
                    )
                }
            }
        }
    }
}
