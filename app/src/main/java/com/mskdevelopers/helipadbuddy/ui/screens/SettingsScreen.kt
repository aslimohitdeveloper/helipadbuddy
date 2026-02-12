package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.ui.components.DisclaimerBanner

@Composable
fun SettingsScreen(
    sinkRateThresholdFtMin: Float,
    onSinkRateThresholdChange: (Float) -> Unit,
    oatCelsius: Float,
    onOatCelsiusChange: (Float) -> Unit,
    nightThresholdLux: Float,
    onNightThresholdLuxChange: (Float) -> Unit,
    unitPreferences: UnitPreferences,
    onAltitudeUnitChange: (Boolean) -> Unit,
    onSpeedUnitChange: (Boolean) -> Unit,
    fieldElevationMeters: Float,
    onFieldElevationMetersChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var sinkText by remember(sinkRateThresholdFtMin) { mutableStateOf(sinkRateThresholdFtMin.toString()) }
    var oatText by remember(oatCelsius) { mutableStateOf(oatCelsius.toString()) }
    var luxText by remember(nightThresholdLux) { mutableStateOf(nightThresholdLux.toString()) }
    var elevationText by remember(fieldElevationMeters) { 
        mutableStateOf(if (fieldElevationMeters > 0f) fieldElevationMeters.toString() else "") 
    }

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        DisclaimerBanner()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Sink rate warning (ft/min)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = sinkText,
                    onValueChange = {
                        sinkText = it
                        it.toFloatOrNull()?.let { v -> onSinkRateThresholdChange(v) }
                    },
                    label = { Text("Threshold") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "OAT for density altitude (°C)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = oatText,
                    onValueChange = {
                        oatText = it
                        it.toFloatOrNull()?.let { v -> onOatCelsiusChange(v) }
                    },
                    label = { Text("Temperature") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Night mode threshold (lux)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = luxText,
                    onValueChange = {
                        luxText = it
                        it.toFloatOrNull()?.let { v -> onNightThresholdLuxChange(v) }
                    },
                    label = { Text("Below this = night") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Field elevation for QNH (meters)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Enter field elevation to calculate QNH. Leave empty to use GPS altitude.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = elevationText,
                    onValueChange = {
                        elevationText = it
                        val elevation = it.toFloatOrNull()
                        if (elevation != null && elevation >= 0f) {
                            onFieldElevationMetersChange(elevation)
                        } else if (it.isEmpty()) {
                            onFieldElevationMetersChange(0f)
                        }
                    },
                    label = { Text("Elevation (m)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Unit preferences",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Altitude: Feet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = unitPreferences.altitudeFeet,
                        onCheckedChange = onAltitudeUnitChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                Text(
                    "Off = meters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Speed: Knots",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = unitPreferences.speedKnots,
                        onCheckedChange = onSpeedUnitChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
                Text(
                    "Off = km/h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
