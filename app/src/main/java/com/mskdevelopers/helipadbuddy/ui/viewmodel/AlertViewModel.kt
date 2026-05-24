package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.mskdevelopers.helipadbuddy.data.model.AlertData
import com.mskdevelopers.helipadbuddy.data.model.AlertSeverity
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.model.RunwayWindData
import com.mskdevelopers.helipadbuddy.data.model.TerrainData
import com.mskdevelopers.helipadbuddy.data.model.VerticalPerformance
import com.mskdevelopers.helipadbuddy.domain.alert.AlertEngine
import com.mskdevelopers.helipadbuddy.domain.alert.AlertType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlin.math.abs

class AlertViewModel(
    private val alertEngine: AlertEngine = AlertEngine()
) : ViewModel() {

    private val _activeAlerts = MutableStateFlow<List<AlertData>>(emptyList())
    val activeAlerts: StateFlow<List<AlertData>> = _activeAlerts.asStateFlow()

    val primaryAlert: StateFlow<AlertData?> = _activeAlerts
        .map { alerts ->
            alerts.maxByOrNull { it.severity.ordinal }
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var previousCrosswindKt = 0f
    private var previousCrosswindSampleMs = 0L

    var alertsEnabled: Boolean = true
    var sinkRateEnabled: Boolean = true
    var terrainEnabled: Boolean = true
    var hardLandingEnabled: Boolean = true
    var gpsWeakEnabled: Boolean = true
    var bankAngleEnabled: Boolean = true
    var excessiveDescentEnabled: Boolean = true

    fun evaluate(
        vertical: VerticalPerformance,
        terrain: TerrainData,
        motion: MotionData,
        gnss: GnssHealthData,
        pressure: PressureData,
        rollDegrees: Float,
        groundSpeedMps: Float = 0f,
        runwayWind: RunwayWindData = RunwayWindData.EMPTY
    ) {
        if (!alertsEnabled) {
            publishAlerts(emptyList())
            return
        }

        val alerts = mutableListOf<AlertData>()
        if (alertEngine.shouldShow(AlertType.SINK_RATE, vertical.sinkRateWarning, sinkRateEnabled)) {
            alerts.add(AlertData(AlertType.SINK_RATE.message, AlertType.SINK_RATE.severity))
        }
        val terrainAlert = terrain.isAvailable &&
            terrain.warningLevel != TerrainData.TerrainWarning.NONE
        if (alertEngine.shouldShow(AlertType.TERRAIN, terrainAlert, terrainEnabled)) {
            alerts.add(AlertData(AlertType.TERRAIN.message, AlertType.TERRAIN.severity))
        }
        if (alertEngine.shouldShow(AlertType.HARD_LANDING, motion.hardLandingDetected, hardLandingEnabled)) {
            alerts.add(AlertData(AlertType.HARD_LANDING.message, AlertType.HARD_LANDING.severity))
        }
        val gpsWeak = gnss.quality == GnssHealthData.GnssQuality.POOR ||
            gnss.quality == GnssHealthData.GnssQuality.NO_FIX
        if (alertEngine.shouldShow(AlertType.GPS_WEAK, gpsWeak, gpsWeakEnabled)) {
            alerts.add(AlertData(AlertType.GPS_WEAK.message, AlertType.GPS_WEAK.severity))
        }

        val bankActive = groundSpeedMps > 0.514f
        if (bankActive && bankAngleEnabled) {
            val absRoll = abs(rollDegrees)
            when {
                absRoll >= 45f && alertEngine.shouldShow(AlertType.BANK_CRITICAL, true, bankAngleEnabled) ->
                    alerts.add(AlertData(AlertType.BANK_CRITICAL.message, AlertType.BANK_CRITICAL.severity))
                absRoll >= 30f && alertEngine.shouldShow(AlertType.BANK_WARNING, true, bankAngleEnabled) ->
                    alerts.add(AlertData(AlertType.BANK_WARNING.message, AlertType.BANK_WARNING.severity))
                absRoll >= 20f && alertEngine.shouldShow(AlertType.BANK_CAUTION, true, bankAngleEnabled) ->
                    alerts.add(AlertData(AlertType.BANK_CAUTION.message, AlertType.BANK_CAUTION.severity))
            }
        } else {
            alertEngine.reset(AlertType.BANK_CAUTION)
            alertEngine.reset(AlertType.BANK_WARNING)
            alertEngine.reset(AlertType.BANK_CRITICAL)
        }

        if (alertEngine.shouldShow(
                AlertType.EXCESSIVE_DESCENT,
                pressure.altitudeTrend10sFtMin < -1000f,
                excessiveDescentEnabled
            )
        ) {
            alerts.add(AlertData(AlertType.EXCESSIVE_DESCENT.message, AlertType.EXCESSIVE_DESCENT.severity))
        }

        val crosswindIncreasing = detectCrosswindIncreasing(runwayWind.absCrosswindKt)
        if (alertEngine.shouldShow(AlertType.CROSSWIND_INCREASING, crosswindIncreasing, alertsEnabled)) {
            alerts.add(AlertData(AlertType.CROSSWIND_INCREASING.message, AlertType.CROSSWIND_INCREASING.severity))
        }

        publishAlerts(alerts)
    }

    private fun publishAlerts(alerts: List<AlertData>) {
        val sorted = alerts.sortedByDescending { it.severity.ordinal }
        val current = _activeAlerts.value
        if (sorted.size == current.size &&
            sorted.zip(current).all { (a, b) -> a.message == b.message && a.severity == b.severity }
        ) {
            return
        }
        _activeAlerts.value = sorted
    }

    private fun detectCrosswindIncreasing(currentCrosswindKt: Float): Boolean {
        val now = System.currentTimeMillis()
        val increasing = currentCrosswindKt > 5f &&
            currentCrosswindKt - previousCrosswindKt >= 2f &&
            now - previousCrosswindSampleMs < 8_000L
        previousCrosswindKt = currentCrosswindKt
        previousCrosswindSampleMs = now
        return increasing
    }
}
