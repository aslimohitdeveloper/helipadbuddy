package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalRed

@Composable
fun MotionCard(
    motion: MotionData,
    modifier: Modifier = Modifier
) {
    val turnText = String.format("%.1f", motion.turnRateDegPerSec)
    AviationInstrumentCard(style = InstrumentStyle.MOTION, modifier = modifier, contentPadding = 8.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("TURN / G-LOAD", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(start = 6.dp))
            if (motion.hardLandingDetected) {
                Canvas(modifier = Modifier.size(8.dp).padding(start = 6.dp)) {
                    drawCircle(color = SignalRed, radius = 4f, center = Offset(size.width / 2f, size.height / 2f))
                }
            }
        }
        Column {
            Text("Turn Rate", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
            Text(
                "$turnText°/s",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                "+G: %.2f  −G: %.2f".format(motion.gLoadPositive, motion.gLoadNegative),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                "Peak +G: %.2f".format(motion.gLoadPeakPositive),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                "Peak −G: %.2f".format(motion.gLoadPeakNegative),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
        if (motion.hardLandingDetected) {
            Text("HARD LANDING", style = MaterialTheme.typography.labelSmall, color = SignalRed, maxLines = 1)
        }
    }
}
