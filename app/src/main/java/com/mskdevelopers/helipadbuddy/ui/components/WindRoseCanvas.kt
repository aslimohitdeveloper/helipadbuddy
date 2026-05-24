package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindRoseCanvas(
    windFromDegrees: Int,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    ringColor: Color = Color.White.copy(alpha = 0.45f),
    arrowColor: Color = SignalBlue,
    showFromLabel: Boolean = true
) {
    val fromDeg = ((windFromDegrees % 360) + 360) % 360
    val animatedFromDeg by animateFloatAsState(
        targetValue = fromDeg.toFloat(),
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "wind_rose"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = minOf(this.size.width, this.size.height) / 2f - 6f
            drawCircle(color = ringColor, radius = radius, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f))
            for (deg in 0 until 360 step 30) {
                val rad = meteorologicalDegreesToCanvasRadians(deg.toFloat())
                val outer = offsetOnCompass(center, radius, rad)
                val inner = offsetOnCompass(center, radius - if (deg % 90 == 0) 10f else 6f, rad)
                drawLine(
                    color = ringColor,
                    start = inner,
                    end = outer,
                    strokeWidth = if (deg % 90 == 0) 2.5f else 1.5f
                )
            }
            val arrowRad = meteorologicalDegreesToCanvasRadians(animatedFromDeg)
            val tip = offsetOnCompass(center, radius * 0.78f, arrowRad)
            val path = Path().apply {
                moveTo(tip.x, tip.y)
                val left = offsetOnCompass(center, radius * 0.22f, arrowRad + Math.PI.toFloat() * 0.75f)
                val right = offsetOnCompass(center, radius * 0.22f, arrowRad - Math.PI.toFloat() * 0.75f)
                lineTo(left.x, left.y)
                lineTo(right.x, right.y)
                close()
            }
            drawPath(path, arrowColor)
            drawCircle(color = arrowColor, radius = 4f, center = center)
        }
        if (showFromLabel) {
            Text(
                "FROM $fromDeg°",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Aviation bearing: 0° = North, clockwise. Canvas y increases downward. */
private fun meteorologicalDegreesToCanvasRadians(metDeg: Float): Double {
    val normalized = ((metDeg % 360f) + 360f) % 360f
    return Math.toRadians((90.0 - normalized).toDouble())
}

private fun offsetOnCompass(center: Offset, distance: Float, rad: Double): Offset {
    return Offset(
        center.x + distance * cos(rad).toFloat(),
        center.y - distance * sin(rad).toFloat()
    )
}
