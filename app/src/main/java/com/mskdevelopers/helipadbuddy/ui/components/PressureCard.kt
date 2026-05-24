package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.PressureData

@Composable
fun PressureCard(
    pressure: PressureData,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.PRESSURE, modifier = modifier) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Compress, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("PRESSURE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(start = 6.dp))
        }
        if (pressure.qfeHpa <= 0f) {
            Text("No barometer", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.7f))
        } else {
            Text(
                "QFE: %.1f hPa".format(pressure.qfeHpa),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Text("QNH: %.1f hPa".format(pressure.qnhHpa), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        val trend = when (pressure.trendDirection) {
            PressureData.TrendDirection.CLIMBING -> "↑"
            PressureData.TrendDirection.DESCENDING -> "↓"
            PressureData.TrendDirection.LEVEL -> "→"
        }
        if (pressure.qfeHpa > 0f) {
            Text(
                "Trend: $trend (10s: %.0f ft/min)".format(pressure.altitudeTrend10sFtMin),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
