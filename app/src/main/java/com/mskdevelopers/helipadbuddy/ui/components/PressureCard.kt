package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue

@Composable
fun PressureCard(
    pressure: PressureData,
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
                    Icons.Outlined.Compress,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "PRESSURE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            if (pressure.qfeHpa <= 0f) {
                Text(
                    "No barometer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "QFE: %.1f hPa".format(pressure.qfeHpa),
                    style = MaterialTheme.typography.headlineSmall,
                    color = SignalBlue // Blue for primary data
                )
            }
            Text(
                "QNH: %.1f hPa".format(pressure.qnhHpa),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val trend = when (pressure.trendDirection) {
                PressureData.TrendDirection.CLIMBING -> "↑"
                PressureData.TrendDirection.DESCENDING -> "↓"
                PressureData.TrendDirection.LEVEL -> "→"
            }
            if (pressure.qfeHpa > 0f) {
                Text(
                    "Trend: %s (10s: %.0f ft/min)".format(trend, pressure.altitudeTrend10sFtMin),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
