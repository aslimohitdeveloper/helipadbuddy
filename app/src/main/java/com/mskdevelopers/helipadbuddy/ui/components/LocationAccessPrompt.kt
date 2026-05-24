package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class LocationAccessIssue {
    PERMISSION_REQUIRED,
    PERMISSION_PERMANENTLY_DENIED,
    LOCATION_DISABLED
}

@Composable
fun LocationAccessPrompt(
    issue: LocationAccessIssue,
    onAllowLocation: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onEnableLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (title, message, primaryLabel, onPrimary) = when (issue) {
        LocationAccessIssue.PERMISSION_REQUIRED -> PromptCopy(
            title = "Allow location access",
            message = "Helipad Buddy needs your location for GPS position, altitude, ground speed, and METAR weather near you.",
            primaryLabel = "Allow",
            onPrimary = onAllowLocation
        )
        LocationAccessIssue.PERMISSION_PERMANENTLY_DENIED -> PromptCopy(
            title = "Location permission required",
            message = "Location was denied. Open app settings and allow Location so GPS and weather can work.",
            primaryLabel = "Open Settings",
            onPrimary = onOpenAppSettings
        )
        LocationAccessIssue.LOCATION_DISABLED -> PromptCopy(
            title = "Turn on location",
            message = "Device location (GPS) is off. Enable location services to get position, altitude, and GNSS data.",
            primaryLabel = "Turn on location",
            onPrimary = onEnableLocation
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onPrimary) {
                        Text(primaryLabel)
                    }
                    if (issue == LocationAccessIssue.PERMISSION_PERMANENTLY_DENIED) {
                        OutlinedButton(onClick = onAllowLocation) {
                            Text("Try again")
                        }
                    }
                }
            }
        }
    }
}

private data class PromptCopy(
    val title: String,
    val message: String,
    val primaryLabel: String,
    val onPrimary: () -> Unit
)
