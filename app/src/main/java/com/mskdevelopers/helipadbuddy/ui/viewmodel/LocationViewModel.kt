package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.GnssHealthData
import com.mskdevelopers.helipadbuddy.data.model.PositionData
import kotlinx.coroutines.Job
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlin.math.abs

/**
 * Position and navigation from GNSS.
 * Track (GPS course) from location; heading merged from AttitudeViewModel in UI.
 */
class LocationViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val locationRepo get() = sensorRepository.locationRepository

    private val _position = MutableStateFlow(PositionData.EMPTY)
    val position: StateFlow<PositionData> = _position.asStateFlow()

    private val _gnssHealth = MutableStateFlow(GnssHealthData.EMPTY)
    val gnssHealth: StateFlow<GnssHealthData> = _gnssHealth.asStateFlow()

    /** Heading from magnetometer (set by AttitudeViewModel or combined in UI). */
    private val _headingDegrees = MutableStateFlow(0f)
    val headingDegrees: StateFlow<Float> = _headingDegrees.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(true)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled.asStateFlow()

    private var positionJob: Job? = null
    private var locationEnabledJob: Job? = null
    private var gnssJob: Job? = null

    // Smoothing buffers
    private val altitudeHistory = mutableListOf<Double>()
    private val speedHistory = mutableListOf<Float>()
    private val headingHistory = mutableListOf<Float>()
    private val maxHistorySize = 8
    private var smoothedAltitude: Double = 0.0
    private var smoothedSpeed: Float = 0f
    private var smoothedHeading: Float = 0f
    private var smoothedDelta: Float = 0f // For track vs heading delta smoothing

    companion object {
        private const val MIN_SPEED_THRESHOLD_MPS = 0.5f
        private const val DELTA_DEADBAND_DEG = 5f
    }

    fun setHeadingFromAttitude(heading: Float) {
        val normalized = AviationFormulas.normalizeHeading(heading)
        _headingDegrees.value = normalized
        if (smoothedHeading == 0f && normalized != 0f) {
            smoothedHeading = normalized
        } else if (normalized != 0f) {
            val delta = AviationFormulas.angleDifference(normalized, smoothedHeading)
            smoothedHeading = AviationFormulas.normalizeHeading(
                smoothedHeading + 0.12f * delta
            )
        }
    }

    fun startCollecting() {
        positionJob?.cancel()
        locationEnabledJob?.cancel()
        gnssJob?.cancel()
        positionJob = combine(
            locationRepo.positionFlow().onStart { emit(PositionData.EMPTY) },
            _headingDegrees.asStateFlow(),
            _gnssHealth.asStateFlow()
        ) { pos, hdg, gnss ->
            val fixQuality = if (pos.hasFix) gnss.quality.name else "NO_FIX"

            // Smooth MSL altitude only; WGS84 passes through unsmoothed
            val finalMsl = if (pos.hasFix && pos.altitudeMslMeters != 0.0) {
                altitudeHistory.add(pos.altitudeMslMeters)
                if (altitudeHistory.size > maxHistorySize) altitudeHistory.removeAt(0)
                if (altitudeHistory.size >= 3) {
                    altitudeHistory.average()
                } else {
                    pos.altitudeMslMeters
                }
            } else {
                if (smoothedAltitude != 0.0) smoothedAltitude else pos.altitudeMslMeters
            }

            // Smooth speed
            val rawSmoothedSpeed = if (pos.hasFix && pos.groundSpeedMps >= 0f) {
                speedHistory.add(pos.groundSpeedMps)
                if (speedHistory.size > maxHistorySize) speedHistory.removeAt(0)
                if (speedHistory.size >= 3) {
                    AviationFormulas.movingAverage(speedHistory)
                } else {
                    pos.groundSpeedMps
                }
            } else {
                // Fallback to raw or last smoothed value
                if (smoothedSpeed > 0f) smoothedSpeed else pos.groundSpeedMps
            }
            
            // Apply speed threshold: if below 0.5 m/s, set to 0 (device at rest)
            val finalSpeed = if (rawSmoothedSpeed < MIN_SPEED_THRESHOLD_MPS) 0f else rawSmoothedSpeed

            smoothedAltitude = finalMsl
            smoothedSpeed = finalSpeed

            // Use smoothed heading or fallback to track
            val displayHeading = if (smoothedHeading != 0f) {
                smoothedHeading
            } else if (hdg == 0f && pos.hasFix) {
                AviationFormulas.normalizeDegrees(pos.trackDegrees)
            } else {
                hdg
            }

            // Convert smoothed speed to knots/kmh
            val finalSpeedKnots = AviationFormulas.mpsToKnots(finalSpeed)
            val finalSpeedKmh = AviationFormulas.mpsToKmh(finalSpeed)
            val finalMslFeet = AviationFormulas.metersToFeet(finalMsl)

            // Calculate and smooth track vs heading delta (Δ)
            val rawDelta = AviationFormulas.trackVsHeadingDegrees(pos.trackDegrees, displayHeading)
            val finalDelta = if (finalSpeed < MIN_SPEED_THRESHOLD_MPS) {
                // No meaningful delta when stationary
                0f
            } else {
                // Smooth delta with exponential moving average
                val smoothed = if (smoothedDelta == 0f && rawDelta != 0f) {
                    rawDelta // Initialize
                } else {
                    AviationFormulas.exponentialMovingAverage(rawDelta, smoothedDelta, 0.7f)
                }
                smoothedDelta = smoothed
                // Apply deadband: if delta < 5°, set to 0°
                if (abs(smoothed) < DELTA_DEADBAND_DEG) 0f else smoothed
            }

            pos.copy(
                altitudeMslMeters = finalMsl,
                altitudeMslFeet = finalMslFeet,
                altitudeWgs84Meters = pos.altitudeWgs84Meters,
                altitudeWgs84Feet = pos.altitudeWgs84Feet,
                groundSpeedMps = finalSpeed,
                groundSpeedKnots = finalSpeedKnots,
                groundSpeedKmh = finalSpeedKmh,
                headingDegrees = displayHeading,
                trackVsHeadingDegrees = finalDelta,
                fixQuality = fixQuality
            )
        }.onEach { _position.value = it }
            .launchIn(viewModelScope)
        locationEnabledJob = locationRepo.locationEnabledFlow()
            .onEach { _isLocationEnabled.value = it }
            .launchIn(viewModelScope)
        gnssJob = locationRepo.gnssStatusFlow()
            .onEach { _gnssHealth.value = it }
            .launchIn(viewModelScope)
    }
}
