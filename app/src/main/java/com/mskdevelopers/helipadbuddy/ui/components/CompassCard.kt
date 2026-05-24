package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import com.mskdevelopers.helipadbuddy.ui.theme.SignalOrange
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassCard(
    position: PositionData,
    gnss: GnssHealthData,
    magneticStrength: Float,
    modifier: Modifier = Modifier
) {
    val labelMuted = Color.White.copy(alpha = 0.85f)
    val labelBright = Color.White.copy(alpha = 0.95f)
    val headingDeg = AviationFormulas.normalizeHeading(position.headingDegrees)
    val directionLabel = AviationFormulas.degreesToDirection(headingDeg)

    val dialRotation = rememberCompassDialRotation(headingDeg)
    val animatedDialRotation by animateFloatAsState(
        targetValue = dialRotation,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "compass_dial"
    )

    val trackDelta = if (position.groundSpeedMps > 0.5f) {
        AviationFormulas.angleDifference(
            AviationFormulas.normalizeHeading(position.trackDegrees),
            headingDeg
        )
    } else {
        0f
    }

    AviationInstrumentCard(style = InstrumentStyle.COMPASS, modifier = modifier, contentPadding = 6.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Explore, contentDescription = null, modifier = Modifier.size(16.dp), tint = labelMuted)
            Text(
                "HDG %.0f°".format(headingDeg),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Text(
                directionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = SignalBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val dialSize = minOf(maxWidth, maxHeight) * 0.85f
            Box(
                modifier = Modifier.size(dialSize),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = animatedDialRotation }
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val r = minOf(size.width, size.height) / 2f - 16f
                    drawCompassDial(
                        center = center,
                        radiusPx = r,
                        ringColor = labelMuted.copy(alpha = 0.5f),
                        tickColor = labelBright
                    )
                }

                if (position.groundSpeedMps > 0.5f) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationZ = trackDelta }
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val r = minOf(size.width, size.height) / 2f - 16f
                        drawTrackIndicator(center, r * 0.5f)
                    }
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val r = minOf(size.width, size.height) / 2f - 16f
                    drawAircraftSymbol(center, r)
                }
            }
        }

        Text(
            "TRK %.0f°  MAG %.0fμT".format(position.trackDegrees, magneticStrength),
            style = MaterialTheme.typography.labelSmall,
            color = labelMuted,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1
        )
    }
}

@Composable
private fun rememberCompassDialRotation(headingDegrees: Float): Float {
    var dialRotation by remember { mutableFloatStateOf(-headingDegrees) }
    LaunchedEffect(headingDegrees) {
        val target = -AviationFormulas.normalizeHeading(headingDegrees)
        val delta = AviationFormulas.angleDifference(target, dialRotation)
        dialRotation += delta
    }
    return dialRotation
}

private fun DrawScope.drawCompassDial(
    center: Offset,
    radiusPx: Float,
    ringColor: Color,
    tickColor: Color
) {
    val cx = center.x
    val cy = center.y

    drawCircle(color = ringColor, radius = radiusPx, center = center, style = Stroke(width = 3f))

    for (deg in 0 until 360 step 10) {
        val rad = Math.toRadians(deg.toDouble())
        val isCardinal = deg % 90 == 0
        val isIntercardinal = deg % 45 == 0 && !isCardinal
        val tickLength = when {
            isCardinal -> 16f
            isIntercardinal -> 12f
            else -> 7f
        }
        val tickWidth = when {
            isCardinal -> 3f
            isIntercardinal -> 2.5f
            else -> 1.5f
        }
        val outerX = cx + radiusPx * cos(rad).toFloat()
        val outerY = cy - radiusPx * sin(rad).toFloat()
        val innerR = radiusPx - tickLength
        val innerX = cx + innerR * cos(rad).toFloat()
        val innerY = cy - innerR * sin(rad).toFloat()
        drawLine(
            color = tickColor.copy(alpha = if (isCardinal || isIntercardinal) 1f else 0.7f),
            start = Offset(innerX, innerY),
            end = Offset(outerX, outerY),
            strokeWidth = tickWidth
        )
    }
}

private fun DrawScope.drawAircraftSymbol(center: Offset, radiusPx: Float) {
    val cx = center.x
    val cy = center.y
    drawCircle(color = SignalBlue, radius = 5f, center = center)
    val needleLength = radiusPx * 0.65f
    val needleWidth = 10f
    val tailLength = 18f
    val path = Path().apply {
        moveTo(cx, cy - needleLength)
        lineTo(cx - needleWidth, cy - needleLength + 18f)
        lineTo(cx - 4f, cy + tailLength)
        lineTo(cx, cy + tailLength + 4f)
        lineTo(cx + 4f, cy + tailLength)
        lineTo(cx + needleWidth, cy - needleLength + 18f)
        close()
    }
    drawPath(path, SignalBlue)
}

private fun DrawScope.drawTrackIndicator(center: Offset, length: Float) {
    val cx = center.x
    val cy = center.y
    drawLine(
        color = SignalOrange.copy(alpha = 0.9f),
        start = Offset(cx, cy),
        end = Offset(cx, cy - length),
        strokeWidth = 3f
    )
    drawCircle(color = SignalOrange, radius = 4f, center = Offset(cx, cy - length))
}
