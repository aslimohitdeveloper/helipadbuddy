package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskdevelopers.helipadbuddy.data.model.PositionData

@Composable
fun SpeedCard(
    position: PositionData,
    speedKnots: Boolean = true,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.SPEED, modifier = modifier) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.Speed, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("GROUND SPEED", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(start = 6.dp))
        }
        Text(
            if (speedKnots) "%.0f kt".format(position.groundSpeedKnots) else "%.0f km/h".format(position.groundSpeedKmh),
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Text(
            if (speedKnots) "%.0f km/h".format(position.groundSpeedKmh) else "%.0f kt".format(position.groundSpeedKnots),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}
