package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.VerticalPerformance
import com.mskdevelopers.helipadbuddy.ui.theme.SignalGreen
import com.mskdevelopers.helipadbuddy.ui.theme.SignalRed

@Composable
fun VsiCard(
    vertical: VerticalPerformance,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.VSI, modifier = modifier) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White.copy(alpha = 0.7f)
            )
            Text(
                "VSI (ft/min)",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 6.dp)
            )
            if (vertical.sinkRateWarning) {
                Canvas(modifier = Modifier.size(8.dp).padding(start = 6.dp)) {
                    drawCircle(color = SignalRed, radius = 4f, center = Offset(size.width / 2f, size.height / 2f))
                }
            }
        }
        Text(
            text = buildString {
                append(
                    when {
                        vertical.smoothedVerticalSpeedFtMin > 50f -> "↑ "
                        vertical.smoothedVerticalSpeedFtMin < -50f -> "↓ "
                        else -> "− "
                    }
                )
                append("%+.0f".format(vertical.smoothedVerticalSpeedFtMin))
            },
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold),
            color = when {
                vertical.sinkRateWarning -> SignalRed
                vertical.isClimbing -> SignalGreen
                else -> Color.White
            }
        )
        if (vertical.sinkRateWarning) {
            Text("SINK RATE", style = MaterialTheme.typography.labelSmall, color = SignalRed)
        }
    }
}
