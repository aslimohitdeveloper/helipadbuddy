package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.AttitudeData

/**
 * Detailed artificial horizon: sky (blue) / ground (brown), pitch lines, roll ring.
 */
@Composable
fun AttitudeIndicator(
    attitude: AttitudeData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        val skyColor = Color(0xFF1E88E5)
        val groundColor = Color(0xFF5D4037)
        val lineColor = MaterialTheme.colorScheme.onSurfaceVariant
        val primaryColor = MaterialTheme.colorScheme.primary
        val pitchDeg = attitude.pitchDegrees
        val rollDeg = attitude.rollDegrees
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                Icons.Outlined.Flight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "ATTITUDE  HDG %.0f°".format(attitude.headingDegrees),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(8.dp)
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2
            val cy = h / 2
            val radius = minOf(w, h) / 2f
            val pitchScale = radius / 30f

            // Roll: rotate entire scene
            rotate(rollDeg, Offset(cx, cy)) {
                // Horizon line offset by pitch (pitch moves horizon up/down)
                val horizonOffset = pitchDeg * pitchScale
                val skyTop = cy - radius - horizonOffset
                val groundBottom = cy + radius - horizonOffset
                drawRect(groundColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(w, groundBottom.coerceIn(0f, h)))
                drawRect(skyColor, topLeft = Offset(0f, skyTop.coerceIn(0f, h)), size = androidx.compose.ui.geometry.Size(w, (h - skyTop).coerceIn(0f, h)))
                // Pitch lines
                for (p in -30..30 step 10) {
                    if (p == 0) continue
                    val y = cy - horizonOffset + p * pitchScale
                    if (y in 0f..h) {
                        val lineW = (radius * 0.6f * (1 - kotlin.math.abs(p) / 60f)).coerceAtLeast(20f)
                        drawLine(lineColor, Offset(cx - lineW / 2, y), Offset(cx + lineW / 2, y), strokeWidth = 2f)
                    }
                }
                drawLine(Color.White, Offset(cx - radius * 0.5f, cy - horizonOffset), Offset(cx + radius * 0.5f, cy - horizonOffset), strokeWidth = 3f)
            }
            // Fixed aircraft symbol (triangle at center)
            val path = Path().apply {
                moveTo(cx, cy - 12f)
                lineTo(cx - 10f, cy + 10f)
                lineTo(cx + 10f, cy + 10f)
                close()
            }
            drawPath(path, primaryColor)
        }
    }
}
