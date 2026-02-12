package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.ui.theme.SignalGreen
import com.mskdevelopers.helipadbuddy.ui.theme.SignalOrange
import com.mskdevelopers.helipadbuddy.ui.theme.SignalRed

@Composable
fun GnssStatusCard(
    gnss: GnssHealthData,
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
                    Icons.Outlined.SatelliteAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "GNSS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                "In view: %d  Used: %d".format(gnss.satellitesInView, gnss.satellitesUsedInFix),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "GPS %d  Galileo %d  GLONASS %d  BeiDou %d".format(
                    gnss.gpsCount, gnss.galileoCount, gnss.glonassCount, gnss.beidouCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Avg SNR: %.1f dB-Hz".format(gnss.averageSnrDbHz),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Quality: %s".format(gnss.quality.name),
                style = MaterialTheme.typography.labelSmall,
                color = when (gnss.quality) {
                    GnssHealthData.GnssQuality.GOOD -> SignalGreen
                    GnssHealthData.GnssQuality.MARGINAL -> SignalOrange
                    GnssHealthData.GnssQuality.POOR,
                    GnssHealthData.GnssQuality.NO_FIX -> SignalRed
                }
            )
        }
    }
}
