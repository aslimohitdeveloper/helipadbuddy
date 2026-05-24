package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.RunwayConfig
import com.mskdevelopers.helipadbuddy.data.model.RunwayWindData
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
    onNavigateToSensorHealth: () -> Unit = {},
    backgroundMonitoring: Boolean = false,
    onBackgroundMonitoringChange: (Boolean) -> Unit = {},
    runwayConfigs: List<RunwayConfig> = emptyList(),
    activeRunway: RunwayConfig? = null,
    onAddRunway: (String, String, Int?, String?) -> Unit = { _, _, _, _ -> },
    onUpdateRunway: (RunwayConfig) -> Unit = {},
    onSetActiveRunway: (String) -> Unit = {},
    onSetActiveRunwayEnd: (String, String) -> Unit = { _, _ -> },
    onRemoveRunway: (String) -> Unit = {},
    preferredMetarIcao: String? = null,
    onPreferredMetarIcaoChange: (String?) -> Unit = {},
    nearestStationIcao: String? = null,
    nearestStationDistanceKm: Double? = null,
    onSearchMetarStations: (String) -> List<Pair<String, String>> = { emptyList() },
    modifier: Modifier = Modifier
) {
    var sinkText by remember(sinkRateThresholdFtMin) { mutableStateOf(sinkRateThresholdFtMin.toString()) }
    var oatText by remember(oatCelsius) { mutableStateOf(oatCelsius.toString()) }
    var luxText by remember(nightThresholdLux) { mutableStateOf(nightThresholdLux.toString()) }
    var elevationText by remember(fieldElevationMeters) {
        mutableStateOf(if (fieldElevationMeters > 0f) fieldElevationMeters.toString() else "")
    }
    var runwayInput by remember { mutableStateOf("") }
    var selectedActiveEnd by remember { mutableStateOf("") }
    var lengthInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var editingRunway by remember { mutableStateOf<RunwayConfig?>(null) }
    var manualIcao by remember(preferredMetarIcao) {
        mutableStateOf(preferredMetarIcao.orEmpty())
    }
    var useManualIcao by remember(preferredMetarIcao) {
        mutableStateOf(!preferredMetarIcao.isNullOrBlank())
    }
    var icaoSuggestions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

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
                    "Weather station (METAR)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Open-Meteo forecast always uses GPS coordinates. METAR enhancement uses the selected ICAO or nearest station.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = !useManualIcao, onClick = {
                        useManualIcao = false
                        manualIcao = ""
                        onPreferredMetarIcaoChange(null)
                    })
                    Text("Auto (nearest METAR station)", style = MaterialTheme.typography.bodyMedium)
                }
                if (!useManualIcao && !nearestStationIcao.isNullOrBlank()) {
                    val dist = nearestStationDistanceKm?.let { " • %.0f km away".format(it) }.orEmpty()
                    Text(
                        "Suggested: $nearestStationIcao$dist",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 48.dp, bottom = 8.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = useManualIcao, onClick = { useManualIcao = true })
                    Text("Manual ICAO", style = MaterialTheme.typography.bodyMedium)
                }
                if (useManualIcao) {
                    OutlinedTextField(
                        value = manualIcao,
                        onValueChange = {
                            val upper = it.uppercase().filter { ch -> ch.isLetter() }.take(4)
                            manualIcao = upper
                            icaoSuggestions = onSearchMetarStations(upper)
                            if (upper.length == 4) {
                                onPreferredMetarIcaoChange(upper)
                            }
                        },
                        label = { Text("ICAO (4 letters)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    icaoSuggestions.take(6).forEach { (icao, name) ->
                        Text(
                            "$icao — $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Runway Configuration",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Add runway pairs (e.g. 09/27) and select the runway in use.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = runwayInput,
                    onValueChange = { runwayInput = it.uppercase().filter { ch -> ch.isDigit() || ch == '/' } },
                    label = { Text("Runway") },
                    placeholder = { Text("09/27") },
                    modifier = Modifier.fillMaxWidth()
                )
                val end1 = runwayInput.substringBefore("/").trim()
                val end2 = runwayInput.substringAfter("/").trim()
                LaunchedEffect(end1, end2) {
                    if (end1.isNotEmpty() && (selectedActiveEnd.isEmpty() || (selectedActiveEnd != end1 && selectedActiveEnd != end2))) {
                        selectedActiveEnd = end1
                    }
                }
                if (end1.isNotEmpty() && end2.isNotEmpty()) {
                    val heading1 = end1.toIntOrNull()?.times(10)
                    val heading2 = end2.toIntOrNull()?.times(10)
                    Text(
                        "$end1 → ${heading1 ?: "?"}°    $end2 → ${heading2 ?: "?"}°",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        "Active runway end",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedActiveEnd == end1,
                            onClick = { selectedActiveEnd = end1 }
                        )
                        Text(end1, modifier = Modifier.padding(end = 16.dp))
                        RadioButton(
                            selected = selectedActiveEnd == end2,
                            onClick = { selectedActiveEnd = end2 }
                        )
                        Text(end2)
                    }
                }
                OutlinedTextField(
                    value = lengthInput,
                    onValueChange = { lengthInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Length (meters, optional)") },
                    placeholder = { Text("1200") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Grass, slope, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        if (runwayInput.isNotBlank()) {
                            val end = selectedActiveEnd.ifBlank { runwayInput.substringBefore("/") }
                            val length = lengthInput.toIntOrNull()
                            onAddRunway(runwayInput.trim(), end.trim(), length, notesInput.trim().ifEmpty { null })
                            runwayInput = ""
                            selectedActiveEnd = ""
                            lengthInput = ""
                            notesInput = ""
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Add Runway")
                }
                runwayConfigs.forEach { runway ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    val isEditing = editingRunway?.runwayName == runway.runwayName
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(runway.runwayName, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Active: ${runway.activeRunway} (${runway.activeHeadingDeg}°)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${runway.runwayName.substringBefore("/")} → ${runway.heading1}°  " +
                                        "${runway.runwayName.substringAfter("/")} → ${runway.heading2}°",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                runway.runwayLengthMeters?.let { len ->
                                    Text(
                                        "Length: ${len}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                runway.notes?.let { note ->
                                    Text(
                                        note,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Button(onClick = {
                                editingRunway = if (isEditing) null else runway
                            }) {
                                Text(if (isEditing) "Close" else "Edit")
                            }
                        }
                        if (isEditing) {
                            var editEnd by remember(runway) { mutableStateOf(runway.activeRunway) }
                            var editLength by remember(runway) {
                                mutableStateOf(runway.runwayLengthMeters?.toString().orEmpty())
                            }
                            var editNotes by remember(runway) { mutableStateOf(runway.notes.orEmpty()) }
                            OutlinedTextField(
                                value = editEnd,
                                onValueChange = { editEnd = it.uppercase() },
                                label = { Text("Active end") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            OutlinedTextField(
                                value = editLength,
                                onValueChange = { editLength = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Length (m)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                            OutlinedTextField(
                                value = editNotes,
                                onValueChange = { editNotes = it },
                                label = { Text("Notes") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                            Button(
                                onClick = {
                                    val updated = runway.copy(
                                        activeRunway = editEnd,
                                        runwayLengthMeters = editLength.toIntOrNull(),
                                        notes = editNotes.trim().ifEmpty { null }
                                    )
                                    onUpdateRunway(updated)
                                    editingRunway = null
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text("Save")
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val end1 = runway.runwayName.substringBefore("/")
                            val end2 = runway.runwayName.substringAfter("/")
                            RadioButton(
                                selected = runway.activeRunway == end1,
                                onClick = { onSetActiveRunwayEnd(runway.runwayName, end1) }
                            )
                            Text(end1, modifier = Modifier.padding(end = 12.dp))
                            RadioButton(
                                selected = runway.activeRunway == end2,
                                onClick = { onSetActiveRunwayEnd(runway.runwayName, end2) }
                            )
                            Text(end2, modifier = Modifier.padding(end = 12.dp))
                            Button(onClick = { onSetActiveRunway(runway.runwayName) }) {
                                Text(if (activeRunway?.runwayName == runway.runwayName) "Active" else "Select")
                            }
                            Button(onClick = { onRemoveRunway(runway.runwayName) }) {
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Sensor calibration", style = MaterialTheme.typography.titleSmall)
                Button(onClick = onNavigateToSensorHealth, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Open Sensor Health")
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Background monitoring", style = MaterialTheme.typography.titleSmall)
                Switch(
                    checked = backgroundMonitoring,
                    onCheckedChange = onBackgroundMonitoringChange
                )
            }
        }
    }
}
