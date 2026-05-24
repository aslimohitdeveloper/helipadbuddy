package com.mskdevelopers.helipadbuddy.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.AlertData
import com.mskdevelopers.helipadbuddy.data.model.AlertSeverity

@Composable
fun AlertBanner(
    alerts: List<AlertData>,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = alerts.firstOrNull(),
        modifier = modifier.fillMaxWidth(),
        transitionSpec = {
            (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
        },
        label = "alert_banner"
    ) { primary ->
        if (primary != null) {
            AlertBannerItem(alert = primary, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun AlertChipsRow(
    alerts: List<AlertData>,
    modifier: Modifier = Modifier
) {
    if (alerts.isEmpty()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        alerts.take(4).forEach { alert ->
            AlertChip(alert = alert)
        }
    }
}

@Composable
private fun AlertBannerItem(
    alert: AlertData,
    modifier: Modifier = Modifier
) {
    val colors = severityColors(alert.severity)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.container,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = alert.severity.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onContainer
            )
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onContainer
            )
        }
    }
}

@Composable
private fun AlertChip(alert: AlertData) {
    val colors = severityColors(alert.severity)
    Text(
        text = "${alert.severity.name}: ${alert.message}",
        modifier = Modifier
            .background(colors.container, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = colors.onContainer,
        fontWeight = FontWeight.Medium
    )
}

private data class SeverityColors(val container: Color, val onContainer: Color)

private fun severityColors(severity: AlertSeverity): SeverityColors = when (severity) {
    AlertSeverity.CRITICAL -> SeverityColors(Color(0xFF8B0000), Color.White)
    AlertSeverity.WARNING -> SeverityColors(Color(0xFFB71C1C), Color.White)
    AlertSeverity.CAUTION -> SeverityColors(Color(0xFFF57F17), Color(0xFF212121))
    AlertSeverity.INFO -> SeverityColors(Color(0xFF1565C0), Color.White)
}
