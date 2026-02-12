package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.VerticalPerformance
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.domain.calculation.PressureCalculations
import com.mskdevelopers.helipadbuddy.domain.calculation.VerticalSpeedCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlin.math.abs

/**
 * Vertical speed and climb/descent from barometer; sink rate warning.
 */
class VerticalPerformanceViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _verticalPerformance = MutableStateFlow(VerticalPerformance.EMPTY)
    val verticalPerformance: StateFlow<VerticalPerformance> = _verticalPerformance.asStateFlow()

    /** Sink rate threshold (ft/min) for warning; configurable in settings. */
    private val _sinkRateThresholdFtMin = MutableStateFlow(700f)
    val sinkRateThresholdFtMin: StateFlow<Float> = _sinkRateThresholdFtMin.asStateFlow()
    fun setSinkRateThreshold(ftMin: Float) {
        _sinkRateThresholdFtMin.value = ftMin
    }

    private var lastPressureHpa: Float = 0f
    private var lastTimeMs: Long = 0
    private val vsiHistory = ArrayDeque<Float>(40) // Increased from 20 for better smoothing
    private var lastSmoothedVsi: Float = 0f // For exponential moving average

    companion object {
        private const val VSI_DEADBAND_FT_MIN = 25f
        private const val MIN_TIME_DELTA_SEC = 0.5f
    }

    fun startCollecting() {
        sensorRepository.pressureRepository.pressureFlow()
            .onEach { hpa ->
                val now = System.currentTimeMillis()
                if (lastTimeMs > 0 && lastPressureHpa > 0f) {
                    val paNow = PressureCalculations.pressureAltitudeMeters(hpa)
                    val paPrev = PressureCalculations.pressureAltitudeMeters(lastPressureHpa)
                    val dtSec = (now - lastTimeMs) / 1000f
                    // Only process if enough time has passed
                    if (dtSec >= MIN_TIME_DELTA_SEC) {
                        val vsiFtMin = VerticalSpeedCalculator.verticalSpeedFtMin(paNow - paPrev, dtSec)
                        vsiHistory.addLast(vsiFtMin)
                        while (vsiHistory.size > 40) vsiHistory.removeFirst()
                        
                        // Use exponential moving average for smoother transitions
                        val smoothed = if (lastSmoothedVsi == 0f && vsiFtMin != 0f) {
                            vsiFtMin // Initialize
                        } else {
                            AviationFormulas.exponentialMovingAverage(vsiFtMin, lastSmoothedVsi, 0.85f)
                        }
                        lastSmoothedVsi = smoothed
                        
                        // Apply deadband: if within ±25 ft/min, set to 0
                        val finalVsi = if (abs(smoothed) < VSI_DEADBAND_FT_MIN) 0f else smoothed
                        
                        val isClimbing = finalVsi > 50f
                        val sinkWarning = !isClimbing && -finalVsi >= _sinkRateThresholdFtMin.value

                        _verticalPerformance.update {
                            it.copy(
                                verticalSpeedFtMin = vsiFtMin,
                                verticalSpeedMps = VerticalSpeedCalculator.verticalSpeedMps(paNow - paPrev, dtSec),
                                isClimbing = isClimbing,
                                sinkRateWarning = sinkWarning,
                                smoothedVerticalSpeedFtMin = finalVsi, // Use deadband-filtered value
                                timestamp = now
                            )
                        }
                    }
                }
                lastPressureHpa = hpa
                lastTimeMs = now
            }
            .launchIn(viewModelScope)
    }
}
