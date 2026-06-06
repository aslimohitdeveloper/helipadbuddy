package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.AlertData
import com.mskdevelopers.helipadbuddy.data.model.AlertSeverity

@Composable
fun StatusCard(
    primaryAlert: AlertData?,
    modifier: Modifier = Modifier
) {
    AviationInstrumentCard(
        style = InstrumentStyle.NEUTRAL,
        modifier = modifier,
        contentPadding = 6.dp
    ) {
        AnimatedContent(
            targetState = primaryAlert,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "status_card"
        ) { alert ->
            if (alert == null) {
                Text(
                    text = "All systems normal",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50),
                    maxLines = 1
                )
            } else {
                Text(
                    text = "${statusLabel(alert.severity)}: ${alert.message}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = statusColor(alert.severity),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun statusLabel(severity: AlertSeverity): String = when (severity) {
    AlertSeverity.INFO -> "INFO"
    AlertSeverity.CAUTION -> "CAUTION"
    AlertSeverity.WARNING -> "WARNING"
    AlertSeverity.CRITICAL -> "CRITICAL"
}

private fun statusColor(severity: AlertSeverity): Color = when (severity) {
    AlertSeverity.INFO -> Color(0xFF64B5F6)
    AlertSeverity.CAUTION -> Color(0xFFFFC107)
    AlertSeverity.WARNING -> Color(0xFFFF7043)
    AlertSeverity.CRITICAL -> Color(0xFFE53935)
}
