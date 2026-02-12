package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalBlue

@Composable
fun AltitudeCard(
    position: PositionData,
    pressure: PressureData,
    altitudeFeet: Boolean = true,
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
                    Icons.Outlined.Terrain,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "ALTITUDE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                if (altitudeFeet) "WGS84: %.0f ft".format(position.altitudeFeet)
                else "WGS84: %.0f m".format(position.altitudeMeters),
                style = MaterialTheme.typography.headlineMedium,
                color = SignalBlue // Blue for primary altitude
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (altitudeFeet) "Pressure alt: %.0f ft".format(pressure.pressureAltitudeFeet)
                else "Pressure alt: %.0f m".format(pressure.pressureAltitudeMeters),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (altitudeFeet) "Density alt: %.0f ft".format(pressure.densityAltitudeFeet)
                else "Density alt: %.0f m".format(pressure.densityAltitudeMeters),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
