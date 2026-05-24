package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SatelliteAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData

@Composable
fun GnssStatusCard(
    gnss: GnssHealthData,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(style = InstrumentStyle.GNSS, modifier = modifier, contentPadding = 8.dp) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Icon(Icons.Outlined.SatelliteAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.7f))
            Text("GNSS", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.padding(start = 6.dp))
        }
        Text(
            "In view: ${gnss.satellitesInView}  Used: ${gnss.satellitesUsedInFix}",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            "GPS ${gnss.gpsCount}  GAL ${gnss.galileoCount}  GLO ${gnss.glonassCount}  BDS ${gnss.beidouCount}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
        Text(
            "Avg SNR: %.1f dB-Hz".format(gnss.averageSnrDbHz),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}
