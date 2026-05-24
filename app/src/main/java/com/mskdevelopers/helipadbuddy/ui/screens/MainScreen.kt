package com.mskdevelopers.helipadbuddy.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mskdevelopers.helipadbuddy.data.model.AlertData
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.TerrainData
import com.mskdevelopers.helipadbuddy.data.model.UnitPreferences
import com.mskdevelopers.helipadbuddy.data.model.VerticalPerformance
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.ui.components.AltitudeCard
import com.mskdevelopers.helipadbuddy.ui.components.CloudWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.CompassCard
import com.mskdevelopers.helipadbuddy.ui.components.CoordinatesCard
import com.mskdevelopers.helipadbuddy.ui.components.DeveloperInfoCard
import com.mskdevelopers.helipadbuddy.ui.components.DisclaimerBanner
import com.mskdevelopers.helipadbuddy.ui.components.ForecastWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.GnssStatusCard
import com.mskdevelopers.helipadbuddy.ui.components.MetarRawCard
import com.mskdevelopers.helipadbuddy.ui.components.MotionCard
import com.mskdevelopers.helipadbuddy.ui.components.PressureCard
import com.mskdevelopers.helipadbuddy.ui.components.PressureWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.RunwayWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.LocationAccessIssue
import com.mskdevelopers.helipadbuddy.ui.components.LocationAccessPrompt
import com.mskdevelopers.helipadbuddy.ui.components.SensorStatusBanner
import com.mskdevelopers.helipadbuddy.ui.components.SpeedCard
import com.mskdevelopers.helipadbuddy.ui.components.StatusCard
import com.mskdevelopers.helipadbuddy.ui.components.TemperatureWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.VsiCard
import com.mskdevelopers.helipadbuddy.ui.components.VisibilityWeatherCard
import com.mskdevelopers.helipadbuddy.ui.components.WeatherConditionCard
import com.mskdevelopers.helipadbuddy.ui.components.WeatherRefreshBar
import com.mskdevelopers.helipadbuddy.ui.components.WindWeatherCard
import com.mskdevelopers.helipadbuddy.ui.theme.InstrumentGradients

private val TAB_SENSOR = 0
private val TAB_METAR = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    position: PositionData,
    pressure: PressureData,
    vertical: VerticalPerformance,
    gnss: GnssHealthData,
    motion: MotionData,
    magneticStrength: Float,
    terrain: TerrainData = TerrainData.EMPTY,
    runwayDashboard: RunwayDashboardState = RunwayDashboardState.EMPTY,
    widgetWeather: WidgetWeatherData = WidgetWeatherData.EMPTY,
    primaryAlert: AlertData? = null,
    unitPreferences: UnitPreferences = UnitPreferences(),
    hasLocationPermission: Boolean = true,
    isLocationPermissionPermanentlyDenied: Boolean = false,
    hasLocationEnabled: Boolean = true,
    hasPressureSensor: Boolean = true,
    onRequestLocationPermission: () -> Unit = {},
    onOpenAppSettings: () -> Unit = {},
    onOpenLocationSettings: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    nearestStationIcao: String? = null,
    nearestStationDistanceKm: Double? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(TAB_SENSOR) }
    val instrumentInputs = remember(position, pressure, vertical, gnss, motion, terrain, unitPreferences) {
        InstrumentInputs(position, pressure, vertical, gnss, motion, terrain, unitPreferences)
    }
    val speedKnots by remember(unitPreferences.speedKnots) {
        derivedStateOf { unitPreferences.speedKnots }
    }
    val altitudeFeet by remember(unitPreferences.altitudeFeet) {
        derivedStateOf { unitPreferences.altitudeFeet }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(InstrumentGradients.DashboardBackground)
    ) {
        DisclaimerBanner()
        when {
            !hasLocationPermission -> {
                LocationAccessPrompt(
                    issue = if (isLocationPermissionPermanentlyDenied) {
                        LocationAccessIssue.PERMISSION_PERMANENTLY_DENIED
                    } else {
                        LocationAccessIssue.PERMISSION_REQUIRED
                    },
                    onAllowLocation = onRequestLocationPermission,
                    onOpenAppSettings = onOpenAppSettings,
                    onEnableLocation = onOpenLocationSettings
                )
            }
            !hasLocationEnabled -> {
                LocationAccessPrompt(
                    issue = LocationAccessIssue.LOCATION_DISABLED,
                    onAllowLocation = onRequestLocationPermission,
                    onOpenAppSettings = onOpenAppSettings,
                    onEnableLocation = onOpenLocationSettings
                )
            }
        }
        if (!hasPressureSensor) {
            SensorStatusBanner("Barometer unavailable. Altitude and VSI from GPS only.")
        }

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == TAB_SENSOR,
                onClick = { selectedTab = TAB_SENSOR },
                text = { Text("SENSOR DATA", style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedTab == TAB_METAR,
                onClick = { selectedTab = TAB_METAR },
                text = { Text("METAR / WEATHER", style = MaterialTheme.typography.labelSmall) }
            )
        }

        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "dashboard_tabs",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { tab ->
            when (tab) {
                TAB_SENSOR -> SensorDataTab(
                    inputs = instrumentInputs,
                    magneticStrength = magneticStrength,
                    primaryAlert = primaryAlert,
                    speedKnots = speedKnots,
                    altitudeFeet = altitudeFeet
                )
                else -> MetarWeatherTab(
                    inputs = instrumentInputs,
                    runwayDashboard = runwayDashboard,
                    widgetWeather = widgetWeather,
                    terrain = terrain,
                    onRefreshWeather = onRefreshWeather,
                    nearestStationIcao = nearestStationIcao,
                    nearestStationDistanceKm = nearestStationDistanceKm
                )
            }
        }
    }
}

@Composable
private fun SensorDataTab(
    inputs: InstrumentInputs,
    magneticStrength: Float,
    primaryAlert: AlertData?,
    speedKnots: Boolean,
    altitudeFeet: Boolean
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val spacing = 8.dp
        val row2H = 150.dp
        val fixedTotal = 170.dp + row2H + 120.dp + 55.dp + spacing * 3
        val scale = if (maxHeight > fixedTotal) 1f else (maxHeight / fixedTotal).coerceIn(0.75f, 1f)
        val row1 = (170.dp * scale).coerceAtLeast(140.dp)
        val row2 = (row2H * scale).coerceAtLeast(130.dp)
        val row3 = (120.dp * scale).coerceAtLeast(100.dp)
        val row4 = (55.dp * scale).coerceAtLeast(48.dp)
        val contentHeight = row1 + row2 + row3 + row4 + spacing * 3
        val footerH = (maxHeight - contentHeight).coerceAtLeast(96.dp)

        LazyVerticalGrid(
            columns = GridCells.Fixed(12),
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing),
            userScrollEnabled = false,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(0.dp)
        ) {
            item(span = { GridItemSpan(8) }) {
                AltitudeCard(
                    position = inputs.position,
                    pressure = inputs.pressure,
                    terrain = inputs.terrain,
                    altitudeFeet = altitudeFeet,
                    modifier = Modifier.fillMaxWidth().height(row1)
                )
            }
            item(span = { GridItemSpan(4) }) {
                VsiCard(
                    vertical = inputs.vertical,
                    modifier = Modifier.fillMaxWidth().height(row1)
                )
            }
            item(span = { GridItemSpan(4) }) {
                SpeedCard(
                    position = inputs.position,
                    speedKnots = speedKnots,
                    modifier = Modifier.fillMaxWidth().height(row2)
                )
            }
            item(span = { GridItemSpan(4) }) {
                CompassCard(
                    position = inputs.position,
                    gnss = inputs.gnss,
                    magneticStrength = magneticStrength,
                    modifier = Modifier.fillMaxWidth().height(row2)
                )
            }
            item(span = { GridItemSpan(4) }) {
                GnssStatusCard(
                    gnss = inputs.gnss,
                    modifier = Modifier.fillMaxWidth().height(row2)
                )
            }
            item(span = { GridItemSpan(6) }) {
                PressureCard(
                    pressure = inputs.pressure,
                    modifier = Modifier.fillMaxWidth().height(row3)
                )
            }
            item(span = { GridItemSpan(6) }) {
                MotionCard(
                    motion = inputs.motion,
                    modifier = Modifier.fillMaxWidth().height(row3)
                )
            }
            item(span = { GridItemSpan(12) }) {
                StatusCard(
                    primaryAlert = primaryAlert,
                    modifier = Modifier.fillMaxWidth().height(row4)
                )
            }
            item(span = { GridItemSpan(12) }) {
                DeveloperInfoCard(
                    modifier = Modifier.fillMaxWidth().height(footerH)
                )
            }
        }
    }
}

@Composable
private fun MetarWeatherTab(
    inputs: InstrumentInputs,
    runwayDashboard: RunwayDashboardState,
    widgetWeather: WidgetWeatherData,
    terrain: TerrainData,
    onRefreshWeather: () -> Unit,
    nearestStationIcao: String? = null,
    nearestStationDistanceKm: Double? = null
) {
    LaunchedEffect(Unit) {
        if (widgetWeather.updatedAtMillis == 0L && !widgetWeather.isRefreshing) {
            onRefreshWeather()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeatherRefreshBar(
            isRefreshing = widgetWeather.isRefreshing,
            updatedAtMillis = widgetWeather.updatedAtMillis,
            onRefresh = onRefreshWeather
        )

        val spacing = 6.dp
        val coordsH = 72.dp
        val runwayH = 88.dp
        val compactTileH = 92.dp
        val weatherTempTileH = 112.dp
        val pressureCloudTileH = 112.dp
        val forecastH = 152.dp
        val metarRawH = 100.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(12),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(spacing),
            userScrollEnabled = true,
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            contentPadding = PaddingValues(0.dp)
        ) {
            item(span = { GridItemSpan(12) }) {
                CoordinatesCard(
                    position = inputs.position,
                    weather = widgetWeather,
                    terrain = terrain,
                    nearestStationHint = nearestStationIcao,
                    nearestDistanceKm = nearestStationDistanceKm,
                    modifier = Modifier.fillMaxWidth().height(coordsH)
                )
            }
            item(span = { GridItemSpan(12) }) {
                RunwayWeatherCard(
                    state = runwayDashboard,
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(runwayH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                WindWeatherCard(
                    weather = widgetWeather,
                    runwayDashboard = runwayDashboard,
                    modifier = Modifier.fillMaxWidth().height(compactTileH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                VisibilityWeatherCard(
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(compactTileH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                WeatherConditionCard(
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(weatherTempTileH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                TemperatureWeatherCard(
                    weather = widgetWeather,
                    pressure = inputs.pressure,
                    modifier = Modifier.fillMaxWidth().height(weatherTempTileH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                PressureWeatherCard(
                    weather = widgetWeather,
                    pressure = inputs.pressure,
                    modifier = Modifier.fillMaxWidth().height(pressureCloudTileH)
                )
            }
            item(span = { GridItemSpan(6) }) {
                CloudWeatherCard(
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(pressureCloudTileH)
                )
            }
            item(span = { GridItemSpan(12) }) {
                ForecastWeatherCard(
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(forecastH)
                )
            }
            item(span = { GridItemSpan(12) }) {
                MetarRawCard(
                    weather = widgetWeather,
                    modifier = Modifier.fillMaxWidth().height(metarRawH)
                )
            }
        }
    }
}

private operator fun Dp.times(scale: Float): Dp = (this.value * scale).dp

private data class InstrumentInputs(
    val position: PositionData,
    val pressure: PressureData,
    val vertical: VerticalPerformance,
    val gnss: GnssHealthData,
    val motion: MotionData,
    val terrain: TerrainData,
    val unitPreferences: UnitPreferences
)
