package com.mskdevelopers.helipadbuddy

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mskdevelopers.helipadbuddy.ui.theme.HelipadBuddyTheme
import com.mskdevelopers.helipadbuddy.ui.screens.LoggingScreen
import com.mskdevelopers.helipadbuddy.ui.screens.MainScreen
import com.mskdevelopers.helipadbuddy.ui.screens.SettingsScreen
import com.mskdevelopers.helipadbuddy.ui.screens.WeatherRawDataScreen
import com.mskdevelopers.helipadbuddy.ui.viewmodel.AttitudeViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LightViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LocationViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LoggingViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.PressureViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.VerticalPerformanceViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.GnssHealthViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.MotionViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.PreferencesViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.mskdevelopers.helipadbuddy.ui.screens.AnalyticsScreen
import com.mskdevelopers.helipadbuddy.ui.screens.ReplayScreen
import com.mskdevelopers.helipadbuddy.ui.screens.SensorHealthScreen
import com.mskdevelopers.helipadbuddy.ui.viewmodel.AlertViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.AnalyticsViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.ReplayViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.SensorHealthViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.TerrainViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.WidgetSyncViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.WindViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.ViewModelFactory
import com.mskdevelopers.helipadbuddy.util.PermissionsManager
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val permissionResultTrigger = MutableStateFlow(0)

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissionResultTrigger.value++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val permissionTrigger by this@MainActivity.permissionResultTrigger.collectAsState(initial = 0)
            val app = application as HelipadBuddyApplication
            val factory = remember { ViewModelFactory.from(app) }
            val lightViewModel: LightViewModel = viewModel(factory = factory)
            LaunchedEffect(Unit) { lightViewModel.startCollecting() }
            HelipadBuddyTheme(
                darkTheme = true,
                nightMode = false,
                dynamicColor = false
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    App(
                        factory = factory,
                        permissionResultVersion = permissionTrigger,
                        onRequestLocationPermission = { requestLocationPermissionFromUi() },
                        onOpenAppSettings = { openAppSettings() },
                        onOpenLocationSettings = { openLocationSettings() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionResultTrigger.value++
    }

    override fun onPause() {
        super.onPause()
        // Optionally stop sensors when backgrounded (foreground-safe)
    }

    fun requestLocationPermissionFromUi() {
        val needed = PermissionsManager.foregroundLocationPermissionsNeeded(this)
        if (needed.isEmpty()) return
        if (PermissionsManager.isPermissionPermanentlyDenied(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            openAppSettings()
            return
        }
        val launchRequest = {
            PermissionsManager.markLocationPermissionRequested(this)
            requestPermissions.launch(needed)
        }
        if (PermissionsManager.shouldShowLocationRationale(this)) {
            AlertDialog.Builder(this)
                .setTitle("Allow location access")
                .setMessage(
                    "Helipad Buddy needs location access for GPS position, altitude, ground speed, and weather near you. " +
                        "Data is used only for situational awareness on this device."
                )
                .setPositiveButton("Allow") { _, _ -> launchRequest() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            launchRequest()
        }
    }

    fun openAppSettings() {
        startActivity(PermissionsManager.appSettingsIntent(this))
    }

    fun openLocationSettings() {
        startActivity(PermissionsManager.locationSourceSettingsIntent())
    }
}

@Composable
private fun App(
    factory: ViewModelFactory,
    permissionResultVersion: Int,
    onRequestLocationPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val navController = rememberNavController()
    val preferencesViewModel: PreferencesViewModel = viewModel(factory = factory)
    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("main") {
            DashboardScreen(
                factory = factory,
                preferencesViewModel = preferencesViewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToLogging = { navController.navigate("logging") },
                onNavigateToWeatherRaw = { navController.navigate("weather-raw") },
                permissionResultVersion = permissionResultVersion,
                onRequestLocationPermission = onRequestLocationPermission,
                onOpenAppSettings = onOpenAppSettings,
                onOpenLocationSettings = onOpenLocationSettings
            )
        }
        composable("sensor-health") {
            SensorHealthRoute(factory = factory, onBack = { navController.popBackStack() })
        }
        composable(
            route = "replay/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { entry ->
            val sessionId = entry.arguments?.getLong("sessionId") ?: 0L
            ReplayRoute(factory = factory, sessionId = sessionId, onBack = { navController.popBackStack() })
        }
        composable(
            route = "analytics/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { entry ->
            val sessionId = entry.arguments?.getLong("sessionId") ?: 0L
            AnalyticsRoute(factory = factory, sessionId = sessionId, onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsRoute(
                factory = factory,
                preferencesViewModel = preferencesViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToSensorHealth = { navController.navigate("sensor-health") }
            )
        }
        composable("logging") {
            LoggingRoute(
                factory = factory,
                onBack = { navController.popBackStack() },
                onReplaySession = { id -> navController.navigate("replay/$id") },
                onAnalyticsSession = { id -> navController.navigate("analytics/$id") }
            )
        }
        composable("weather-raw") {
            WeatherRawRoute(factory = factory, onBack = { navController.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreen(
    factory: ViewModelFactory,
    preferencesViewModel: PreferencesViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogging: () -> Unit,
    onNavigateToWeatherRaw: () -> Unit,
    permissionResultVersion: Int,
    onRequestLocationPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val hasLocationPermission = PermissionsManager.hasFineLocation(context)
    val isLocationPermissionPermanentlyDenied = remember(hasLocationPermission, permissionResultVersion) {
        !hasLocationPermission && activity != null &&
            PermissionsManager.isPermissionPermanentlyDenied(activity, Manifest.permission.ACCESS_FINE_LOCATION)
    }
    val locationViewModel: LocationViewModel = viewModel(factory = factory)
    val pressureViewModel: PressureViewModel = viewModel(factory = factory)
    val verticalViewModel: VerticalPerformanceViewModel = viewModel(factory = factory)
    val gnssViewModel: GnssHealthViewModel = viewModel(factory = factory)
    val attitudeViewModel: AttitudeViewModel = viewModel(factory = factory)
    val motionViewModel: MotionViewModel = viewModel(factory = factory)
    val lightViewModel: LightViewModel = viewModel(factory = factory)
    val terrainViewModel: TerrainViewModel = viewModel(factory = factory)
    val windViewModel: WindViewModel = viewModel(factory = factory)
    val widgetSyncViewModel: WidgetSyncViewModel = viewModel(factory = factory)
    val alertViewModel: AlertViewModel = viewModel(factory = factory)

    LaunchedEffect(Unit) {
        pressureViewModel.startCollecting()
        verticalViewModel.startCollecting()
        gnssViewModel.startCollecting()
        attitudeViewModel.startCollecting()
        motionViewModel.startCollecting()
        lightViewModel.startCollecting()
    }
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            locationViewModel.startCollecting()
            gnssViewModel.startCollecting()
        }
    }
    LaunchedEffect(permissionResultVersion) {
        if (permissionResultVersion > 0) {
            kotlinx.coroutines.delay(150)
            if (PermissionsManager.hasFineLocation(context)) {
                locationViewModel.startCollecting()
                gnssViewModel.startCollecting()
            }
        }
    }

    val position by locationViewModel.position.collectAsState()
    val isLocationEnabled by locationViewModel.isLocationEnabled.collectAsState()
    val pressure by pressureViewModel.pressureData.collectAsState()
    val vertical by verticalViewModel.verticalPerformance.collectAsState()
    val gnss by gnssViewModel.gnssHealth.collectAsState()
    val attitude by attitudeViewModel.attitude.collectAsState()
    val motion by motionViewModel.motion.collectAsState()
    val magneticStrength by attitudeViewModel.magneticStrength.collectAsState()
    val unitPreferences by preferencesViewModel.unitPreferences.collectAsState()
    val fieldElevationMeters by preferencesViewModel.fieldElevationMeters.collectAsState()
    val terrain by terrainViewModel.terrain.collectAsState()
    val runwayDashboard by windViewModel.runwayDashboard.collectAsState()
    val activeRunway by preferencesViewModel.activeRunway.collectAsState()
    val primaryAlert by alertViewModel.primaryAlert.collectAsState()
    val pressureWidget by pressureViewModel.pressureWidgetData.collectAsState()
    val widgetWeather by widgetSyncViewModel.weather.collectAsState()
    val app = context.applicationContext as HelipadBuddyApplication
    val nearestStation = remember(position.latitude, position.longitude, position.hasFix) {
        if (position.hasFix) {
            app.nearestAirportRepository.findNearest(position.latitude, position.longitude)
        } else {
            null
        }
    }
    val nearestDistanceKm = remember(position.latitude, position.longitude, nearestStation) {
        nearestStation?.let {
            app.nearestAirportRepository.distanceKm(
                position.latitude,
                position.longitude,
                it.latitude,
                it.longitude
            )
        }
    }

    LaunchedEffect(widgetWeather.temperature) {
        if (widgetWeather.temperature != 0f) {
            pressureViewModel.setWeatherTemperatureC(widgetWeather.temperature)
        }
    }

    LaunchedEffect(pressureWidget) {
        if (pressureWidget.qfeHpa > 0f) {
            widgetSyncViewModel.syncPressure(pressureWidget)
        }
    }

    LaunchedEffect(position.groundSpeedKnots) {
        motionViewModel.setGroundSpeedKnots(position.groundSpeedKnots)
    }

    LaunchedEffect(position, gnss, vertical) {
        if (position.hasFix) {
            terrainViewModel.update(
                position.altitudeMslMeters,
                position.latitude,
                position.longitude,
                vertical.smoothedVerticalSpeedFtMin
            )
            windViewModel.update(position.headingDegrees, position.trackDegrees, position.groundSpeedKnots)
            widgetSyncViewModel.syncFromPosition(
                position.latitude, position.longitude, position.altitudeMslMeters,
                gnss.satellitesUsedInFix, position.fixQuality
            )
        }
    }

    LaunchedEffect(activeRunway, widgetWeather.windDirection, widgetWeather.windSpeedKt) {
        windViewModel.updateRunwayWind(
            runway = activeRunway,
            windDirectionDeg = widgetWeather.windDirection,
            windSpeedKts = widgetWeather.windSpeedKt.toFloat()
        )
    }

    LaunchedEffect(runwayDashboard, primaryAlert) {
        val topSeverity = primaryAlert?.severity?.name.orEmpty()
        widgetSyncViewModel.syncRunwayDashboard(runwayDashboard, topSeverity)
    }

    LaunchedEffect(vertical, terrain, motion, gnss, pressure, attitude, position, runwayDashboard) {
        alertViewModel.evaluate(
            vertical = vertical,
            terrain = terrain,
            motion = motion,
            gnss = gnss,
            pressure = pressure,
            rollDegrees = attitude.rollDegrees,
            groundSpeedMps = position.groundSpeedMps,
            runwayWind = runwayDashboard.components
        )
    }

    // Update GPS altitude for QNH calculation (only if user hasn't set field elevation)
    LaunchedEffect(position, fieldElevationMeters) {
        if (fieldElevationMeters <= 0f) {
            pressureViewModel.setGpsAltitudeMeters(position.altitudeMslMeters.toFloat())
        }
    }
    
    // Update field elevation in PressureViewModel when it changes
    LaunchedEffect(fieldElevationMeters) {
        pressureViewModel.setFieldElevationMeters(fieldElevationMeters)
    }
    LaunchedEffect(attitude) {
        locationViewModel.setHeadingFromAttitude(attitude.headingDegrees)
    }

    val loggingViewModel: LoggingViewModel = viewModel(factory = factory)
    val activeSessionId by loggingViewModel.activeSessionId.collectAsState()
    var latest by remember { mutableStateOf(Triple(position, vertical, motion)) }
    SideEffect { latest = Triple(position, vertical, motion) }
    LaunchedEffect(activeSessionId) {
        if (activeSessionId == null) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(1000L)
            val (pos, v, m) = latest
            loggingViewModel.recordPoint(
                pos.altitudeMeters,
                pos.groundSpeedKnots,
                pos.headingDegrees,
                v.smoothedVerticalSpeedFtMin,
                m.gLoadPositive - m.gLoadNegative,
                latitude = pos.latitude,
                longitude = pos.longitude,
                altitudeMslMeters = pos.altitudeMslMeters,
                altitudeWgs84Meters = pos.altitudeWgs84Meters
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Helipad Buddy") },
                actions = {
                    IconButton(onClick = onNavigateToWeatherRaw) {
                        Icon(Icons.Outlined.Article, contentDescription = "Raw weather data")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToLogging) {
                        Icon(Icons.Outlined.List, contentDescription = "Log")
                    }
                }
            )
        }
    ) { padding ->
            MainScreen(
                position = position,
                pressure = pressure,
                vertical = vertical,
                gnss = gnss,
                motion = motion,
                magneticStrength = magneticStrength,
                terrain = terrain,
                runwayDashboard = runwayDashboard,
                widgetWeather = widgetWeather,
                primaryAlert = primaryAlert,
                unitPreferences = unitPreferences,
                hasLocationPermission = hasLocationPermission,
                isLocationPermissionPermanentlyDenied = isLocationPermissionPermanentlyDenied,
                hasLocationEnabled = isLocationEnabled,
                hasPressureSensor = app.sensorRepository.hasPressureSensor(),
                onRequestLocationPermission = onRequestLocationPermission,
                onOpenAppSettings = onOpenAppSettings,
                onOpenLocationSettings = onOpenLocationSettings,
                nearestStationIcao = nearestStation?.icao,
                nearestStationDistanceKm = nearestDistanceKm,
                onRefreshWeather = {
                    if (position.hasFix) {
                        val pressureArg = if (pressureWidget.qfeHpa > 0f) pressureWidget else null
                        widgetSyncViewModel.refreshNow(
                            latitude = position.latitude,
                            longitude = position.longitude,
                            altitudeMsl = position.altitudeMslMeters.toInt(),
                            sats = gnss.satellitesUsedInFix,
                            gpsQuality = position.fixQuality,
                            pressure = pressureArg
                        )
                    }
                },
                modifier = Modifier.padding(padding)
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherRawRoute(
    factory: ViewModelFactory,
    onBack: () -> Unit
) {
    val widgetSyncViewModel: WidgetSyncViewModel = viewModel(factory = factory)
    val pressureViewModel: PressureViewModel = viewModel(factory = factory)
    val weather by widgetSyncViewModel.weather.collectAsState()
    val pressure by pressureViewModel.pressureData.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Raw weather data") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        WeatherRawDataScreen(
            weather = weather,
            pressure = pressure,
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRoute(
    factory: ViewModelFactory,
    preferencesViewModel: PreferencesViewModel,
    onBack: () -> Unit,
    onNavigateToSensorHealth: () -> Unit
) {
    val pressureViewModel: PressureViewModel = viewModel(factory = factory)
    val verticalViewModel: VerticalPerformanceViewModel = viewModel(factory = factory)
    val lightViewModel: LightViewModel = viewModel(factory = factory)
    val locationViewModel: LocationViewModel = viewModel(factory = factory)
    val widgetSyncViewModel: WidgetSyncViewModel = viewModel(factory = factory)
    val pressureWidget by pressureViewModel.pressureWidgetData.collectAsState()
    val position by locationViewModel.position.collectAsState()
    val gnssViewModel: GnssHealthViewModel = viewModel(factory = factory)
    val gnss by gnssViewModel.gnssHealth.collectAsState()
    val sinkThreshold by verticalViewModel.sinkRateThresholdFtMin.collectAsState()
    val oatCelsius by pressureViewModel.oatCelsius.collectAsState()
    val nightThresh by lightViewModel.nightThresholdLuxState.collectAsState()
    val unitPreferences by preferencesViewModel.unitPreferences.collectAsState()
    val fieldElevationMeters by preferencesViewModel.fieldElevationMeters.collectAsState()
    val backgroundMonitoring by preferencesViewModel.backgroundMonitoring.collectAsState()
    val runwayConfigs by preferencesViewModel.runwayConfigs.collectAsState()
    val activeRunway by preferencesViewModel.activeRunway.collectAsState()
    val preferredMetarIcao by preferencesViewModel.preferredMetarIcao.collectAsState()
    val context = LocalContext.current
    val app = context.applicationContext as HelipadBuddyApplication
    val nearestStation = remember(position.latitude, position.longitude, position.hasFix) {
        if (position.hasFix) {
            app.nearestAirportRepository.findNearest(position.latitude, position.longitude)
        } else {
            null
        }
    }
    val nearestDistanceKm = remember(position.latitude, position.longitude, nearestStation) {
        nearestStation?.let {
            app.nearestAirportRepository.distanceKm(
                position.latitude,
                position.longitude,
                it.latitude,
                it.longitude
            )
        }
    }

    fun refreshWeatherAfterStationChange() {
        if (position.hasFix) {
            val pressureArg = if (pressureWidget.qfeHpa > 0f) pressureWidget else null
            widgetSyncViewModel.refreshNow(
                latitude = position.latitude,
                longitude = position.longitude,
                altitudeMsl = position.altitudeMslMeters.toInt(),
                sats = gnss.satellitesUsedInFix,
                gpsQuality = position.fixQuality,
                pressure = pressureArg
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        SettingsScreen(
            sinkRateThresholdFtMin = sinkThreshold,
            onSinkRateThresholdChange = { verticalViewModel.setSinkRateThreshold(it) },
            oatCelsius = oatCelsius,
            onOatCelsiusChange = { pressureViewModel.setOatCelsius(it) },
            nightThresholdLux = nightThresh,
            onNightThresholdLuxChange = { lightViewModel.setNightThresholdLux(it) },
            unitPreferences = unitPreferences,
            onAltitudeUnitChange = { preferencesViewModel.setAltitudeFeet(it) },
            onSpeedUnitChange = { preferencesViewModel.setSpeedKnots(it) },
            fieldElevationMeters = fieldElevationMeters,
            onFieldElevationMetersChange = { preferencesViewModel.setFieldElevationMeters(it) },
            onNavigateToSensorHealth = onNavigateToSensorHealth,
            backgroundMonitoring = backgroundMonitoring,
            onBackgroundMonitoringChange = { enabled ->
                preferencesViewModel.setBackgroundMonitoring(enabled)
                if (enabled) SensorMonitorService.start(context) else SensorMonitorService.stop(context)
            },
            runwayConfigs = runwayConfigs,
            activeRunway = activeRunway,
            onAddRunway = { id, end, length, notes ->
                preferencesViewModel.addRunway(id, end, length, notes)
            },
            onUpdateRunway = { preferencesViewModel.updateRunway(it) },
            onSetActiveRunway = { preferencesViewModel.setActiveRunway(it) },
            onSetActiveRunwayEnd = { name, end -> preferencesViewModel.setActiveRunwayEnd(name, end) },
            onRemoveRunway = { preferencesViewModel.removeRunway(it) },
            preferredMetarIcao = preferredMetarIcao,
            onPreferredMetarIcaoChange = { icao ->
                preferencesViewModel.setPreferredMetarIcao(icao)
                refreshWeatherAfterStationChange()
            },
            nearestStationIcao = nearestStation?.icao,
            nearestStationDistanceKm = nearestDistanceKm,
            onSearchMetarStations = { prefix ->
                app.nearestAirportRepository.searchByPrefix(prefix).map { it.icao to it.name }
            },
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoggingRoute(
    factory: ViewModelFactory,
    onBack: () -> Unit,
    onReplaySession: (Long) -> Unit,
    onAnalyticsSession: (Long) -> Unit
) {
    val loggingViewModel: LoggingViewModel = viewModel(factory = factory)
    val sessions by loggingViewModel.sessions.collectAsState()
    val activeSessionId by loggingViewModel.activeSessionId.collectAsState()
    val exportResult by loggingViewModel.exportResult.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logging") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
    LoggingScreen(
        sessions = sessions,
        activeSessionId = activeSessionId,
        exportResult = exportResult,
        onStartSession = { loggingViewModel.startSession() },
        onStopSession = { loggingViewModel.stopSession() },
        onExportSession = { id, ctx -> loggingViewModel.exportToCsv(id, ctx) },
        onClearExportResult = { loggingViewModel.clearExportResult() },
        onReplaySession = onReplaySession,
        onAnalyticsSession = onAnalyticsSession,
        modifier = Modifier.padding(padding)
    )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReplayRoute(factory: ViewModelFactory, sessionId: Long, onBack: () -> Unit) {
    val replayViewModel: ReplayViewModel = viewModel(factory = factory)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Replay") },
                navigationIcon = { Button(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        ReplayScreen(viewModel = replayViewModel, sessionId = sessionId, modifier = Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsRoute(factory: ViewModelFactory, sessionId: Long, onBack: () -> Unit) {
    val analyticsViewModel: AnalyticsViewModel = viewModel(factory = factory)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = { Button(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        AnalyticsScreen(viewModel = analyticsViewModel, sessionId = sessionId, modifier = Modifier.padding(padding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SensorHealthRoute(factory: ViewModelFactory, onBack: () -> Unit) {
    val sensorHealthViewModel: SensorHealthViewModel = viewModel(factory = factory)
    val attitudeViewModel: AttitudeViewModel = viewModel(factory = factory)
    val motionViewModel: MotionViewModel = viewModel(factory = factory)
    LaunchedEffect(Unit) {
        attitudeViewModel.startCollecting()
        motionViewModel.startCollecting()
    }
    val attitude by attitudeViewModel.attitude.collectAsState()
    val motion by motionViewModel.motion.collectAsState()
    val mag by attitudeViewModel.magneticStrength.collectAsState()
    LaunchedEffect(attitude, motion, mag) {
        sensorHealthViewModel.updateFromSensors(
            heading = attitude.headingDegrees,
            magStrength = mag,
            ax = 0f, ay = 0f, az = -9.81f,
            gyroX = 0f, gyroY = 0f, gyroZ = 0f,
            atRest = motion.turnRateDegPerSec < 1f
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sensor Health") },
                navigationIcon = { Button(onClick = onBack) { Text("Back") } }
            )
        }
    ) { padding ->
        SensorHealthScreen(viewModel = sensorHealthViewModel, modifier = Modifier.padding(padding))
    }
}
