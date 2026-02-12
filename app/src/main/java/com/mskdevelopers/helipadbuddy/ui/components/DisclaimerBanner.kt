package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Persistent safety disclaimer. Plan Phase 7.3: Persistent disclaimer banner,
 * clear "not certified" messaging.
 */
@Composable
fun DisclaimerBanner(modifier: Modifier = Modifier) {
    Text(
        "Situational awareness only. Not certified for navigation. Do not use as primary reference.",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
