package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.domain.sensor.SensorDiagnostics
import com.mskdevelopers.helipadbuddy.ui.components.DisclaimerBanner
import com.mskdevelopers.helipadbuddy.ui.viewmodel.SensorHealthViewModel

@Composable
fun SensorHealthScreen(
    viewModel: SensorHealthViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.report.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        DisclaimerBanner()
        SensorRow("Compass", report.compassQuality, "Jitter %.1f° • Mag %.0f μT".format(report.compassJitterDeg, report.magneticStrengthUt))
        if (report.magneticInterference) {
            Text("Magnetic interference detected", color = Color(0xFFE53935), modifier = Modifier.padding(horizontal = 16.dp))
        }
        SensorRow("Accelerometer", report.accelerometerQuality, "Deviation %.2f G".format(report.accelerometerDeviationG))
        SensorRow("Gyroscope", report.gyroscopeQuality, "Drift %.1f°/s at rest".format(report.gyroDriftDegPerSec))
        SensorRow("Barometer", report.barometerQuality, if (report.barometerAvailable) "Available" else "Not available")
    }
}

@Composable
private fun SensorRow(name: String, quality: SensorDiagnostics.Quality, detail: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            QualityChip(quality)
        }
    }
}

@Composable
private fun QualityChip(quality: SensorDiagnostics.Quality) {
    val color = when (quality) {
        SensorDiagnostics.Quality.GOOD -> Color(0xFF4CAF50)
        SensorDiagnostics.Quality.FAIR -> Color(0xFFFFB300)
        SensorDiagnostics.Quality.POOR -> Color(0xFFE53935)
        SensorDiagnostics.Quality.UNAVAILABLE -> Color.Gray
    }
    Text(quality.name, color = color, style = MaterialTheme.typography.labelMedium)
}
