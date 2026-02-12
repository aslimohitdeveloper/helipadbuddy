package com.mskdevelopers.helipadbuddy

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
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
import androidx.compose.foundation.layout.padding
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
import com.mskdevelopers.helipadbuddy.ui.viewmodel.AttitudeViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LightViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LocationViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.LoggingViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.PressureViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.VerticalPerformanceViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.GnssHealthViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.MotionViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.PreferencesViewModel
import com.mskdevelopers.helipadbuddy.ui.viewmodel.ViewModelFactory
import com.mskdevelopers.helipadbuddy.util.PermissionsManager
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val permissionResultTrigger = MutableStateFlow(0)

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationDenied = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == false
        if (fineLocationDenied) {
            if (PermissionsManager.isPermissionPermanentlyDenied(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermanentDenialDialog()
            }
        } else {
            permissionResultTrigger.value++
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestLocationPermissionsIfNeeded()
        setContent {
            val permissionTrigger by this@MainActivity.permissionResultTrigger.collectAsState(initial = 0)
            val app = application as HelipadBuddyApplication
            val factory = remember { ViewModelFactory.from(app) }
            val lightViewModel: LightViewModel = viewModel(factory = factory)
            LaunchedEffect(Unit) { lightViewModel.startCollecting() }
            // Disable red night mode - keep dark theme always
            HelipadBuddyTheme(
                darkTheme = true,
                nightMode = false, // Always false - no red screen
                dynamicColor = true
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    App(factory = factory, permissionResultVersion = permissionTrigger)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permissions when user returns to app
        requestLocationPermissionsIfNeeded()
        // Sensors are started in ViewModels when dashboard is composed
    }

    override fun onPause() {
        super.onPause()
        // Optionally stop sensors when backgrounded (foreground-safe)
    }

    private fun requestLocationPermissionsIfNeeded() {
        val needed = PermissionsManager.locationPermissionsNeeded(this)
        if (needed.isEmpty()) return
        
        // Check if permanently denied - if so, show Settings dialog instead of requesting
        val fineLocationNeeded = needed.contains(android.Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineLocationNeeded && PermissionsManager.isPermissionPermanentlyDenied(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            showPermanentDenialDialog()
            return
        }
        
        if (PermissionsManager.shouldShowLocationRationale(this)) {
            AlertDialog.Builder(this)
                .setTitle("Location permission")
                .setMessage("Helipad Buddy needs location access for GPS position, altitude, ground speed, and track. Data is used only for situational awareness.")
                .setPositiveButton(android.R.string.ok) { _, _ -> requestPermissions.launch(needed) }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            requestPermissions.launch(needed)
        }
    }

    private fun showPermanentDenialDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location permission required")
            .setMessage("Location permission is required for GPS position, altitude, and navigation. Please enable it in Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}

@Composable
private fun App(factory: ViewModelFactory, permissionResultVersion: Int) {
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
                permissionResultVersion = permissionResultVersion
            )
        }
        composable("settings") {
            SettingsRoute(
                factory = factory,
                preferencesViewModel = preferencesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("logging") {
            LoggingRoute(factory = factory, onBack = { navController.popBackStack() })
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
    permissionResultVersion: Int
) {
    val context = LocalContext.current
    val hasLocationPermission = PermissionsManager.hasFineLocation(context)
    val locationViewModel: LocationViewModel = viewModel(factory = factory)
    val pressureViewModel: PressureViewModel = viewModel(factory = factory)
    val verticalViewModel: VerticalPerformanceViewModel = viewModel(factory = factory)
    val gnssViewModel: GnssHealthViewModel = viewModel(factory = factory)
    val attitudeViewModel: AttitudeViewModel = viewModel(factory = factory)
    val motionViewModel: MotionViewModel = viewModel(factory = factory)
    val lightViewModel: LightViewModel = viewModel(factory = factory)

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

    // Update GPS altitude for QNH calculation (only if user hasn't set field elevation)
    LaunchedEffect(position, fieldElevationMeters) {
        if (fieldElevationMeters <= 0f) {
            pressureViewModel.setGpsAltitudeMeters(position.altitudeMeters.toFloat())
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
                m.gLoadPositive - m.gLoadNegative
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Helipad Buddy") },
                actions = {
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
        val app = context.applicationContext as HelipadBuddyApplication
            MainScreen(
                position = position,
                pressure = pressure,
                vertical = vertical,
                gnss = gnss,
                attitude = attitude,
                motion = motion,
                magneticStrength = magneticStrength,
                unitPreferences = unitPreferences,
                hasLocationPermission = hasLocationPermission,
                hasLocationEnabled = isLocationEnabled,
                hasPressureSensor = app.sensorRepository.hasPressureSensor(),
                modifier = Modifier.padding(padding)
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRoute(
    factory: ViewModelFactory,
    preferencesViewModel: PreferencesViewModel,
    onBack: () -> Unit
) {
    val pressureViewModel: PressureViewModel = viewModel(factory = factory)
    val verticalViewModel: VerticalPerformanceViewModel = viewModel(factory = factory)
    val lightViewModel: LightViewModel = viewModel(factory = factory)
    val sinkThreshold by verticalViewModel.sinkRateThresholdFtMin.collectAsState()
    val oatCelsius by pressureViewModel.oatCelsius.collectAsState()
    val nightThresh by lightViewModel.nightThresholdLuxState.collectAsState()
    val unitPreferences by preferencesViewModel.unitPreferences.collectAsState()
    val fieldElevationMeters by preferencesViewModel.fieldElevationMeters.collectAsState()
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
            modifier = Modifier.padding(padding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoggingRoute(factory: ViewModelFactory, onBack: () -> Unit) {
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
        modifier = Modifier.padding(padding)
    )
    }
}
