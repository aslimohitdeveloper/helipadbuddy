package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalRed

@Composable
fun MotionCard(
    motion: MotionData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.RotateRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "TURN / G-LOAD",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
                // Red dot indicator for hard landing
                if (motion.hardLandingDetected) {
                    Canvas(modifier = Modifier.size(8.dp)) {
                        drawCircle(
                            color = SignalRed,
                            radius = 4f,
                            center = Offset(size.width / 2f, size.height / 2f)
                        )
                    }
                }
            }
            Text(
                "Turn rate: %.1f °/s".format(motion.turnRateDegPerSec),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "+G: %.2f  −G: %.2f".format(motion.gLoadPositive, motion.gLoadNegative),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Peak +G: %.2f  Peak −G: %.2f".format(motion.gLoadPeakPositive, motion.gLoadPeakNegative),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (motion.hardLandingDetected) {
                Text(
                    "HARD LANDING",
                    style = MaterialTheme.typography.labelSmall,
                    color = SignalRed
                )
            }
        }
    }
}
