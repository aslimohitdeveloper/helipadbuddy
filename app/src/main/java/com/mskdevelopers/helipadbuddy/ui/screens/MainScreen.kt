package com.mskdevelopers.helipadbuddy.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.AttitudeData
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.data.model.VerticalPerformance
import com.mskdevelopers.helipadbuddy.ui.components.AltitudeCard
import com.mskdevelopers.helipadbuddy.ui.components.AttitudeIndicator
import com.mskdevelopers.helipadbuddy.ui.components.CompassCard
import com.mskdevelopers.helipadbuddy.ui.components.DisclaimerBanner
import com.mskdevelopers.helipadbuddy.ui.components.GnssStatusCard
import com.mskdevelopers.helipadbuddy.ui.components.MotionCard
import com.mskdevelopers.helipadbuddy.ui.components.PressureCard
import com.mskdevelopers.helipadbuddy.ui.components.SensorStatusBanner
import com.mskdevelopers.helipadbuddy.ui.components.SpeedCard
import com.mskdevelopers.helipadbuddy.ui.components.VsiCard

private const val WHATSAPP_URL = "https://wa.me/918930253964"

@Composable
fun MainScreen(
    position: PositionData,
    pressure: PressureData,
    vertical: VerticalPerformance,
    gnss: GnssHealthData,
    attitude: AttitudeData,
    motion: MotionData,
    magneticStrength: Float,
    unitPreferences: UnitPreferences = UnitPreferences(),
    hasLocationPermission: Boolean = true,
    hasLocationEnabled: Boolean = true,
    hasPressureSensor: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxSize()) {
        DisclaimerBanner()
        if (!hasLocationPermission) {
            SensorStatusBanner("Location permission needed for position and GNSS.")
        }
        if (hasLocationPermission && !hasLocationEnabled) {
            SensorStatusBanner("Location is off. Turn on GPS for position and GNSS.")
        }
        if (!hasPressureSensor) {
            SensorStatusBanner("Barometer unavailable. Altitude and VSI from GPS only.")
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AltitudeCard(
                    position = position,
                    pressure = pressure,
                    altitudeFeet = unitPreferences.altitudeFeet,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                VsiCard(
                    vertical = vertical,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SpeedCard(
                    position = position,
                    speedKnots = unitPreferences.speedKnots,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                CompassCard(
                    position = position,
                    gnss = gnss,
                    magneticStrength = magneticStrength,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PressureCard(
                    pressure = pressure,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                GnssStatusCard(
                    gnss = gnss,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AttitudeIndicator(
                    attitude = attitude,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                MotionCard(
                    motion = motion,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Developer: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "WhatsApp +918930253964",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(WHATSAPP_URL))
                        )
                    } catch (_: Exception) { }
                }
            )
        }
    }
}
