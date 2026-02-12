package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.data.repository.MotionRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.sqrt
import kotlin.math.abs

/**
 * Turn rate, G-load (with peak hold), hard landing detection.
 */
class MotionViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _motion = MutableStateFlow(MotionData.EMPTY)
    val motion: StateFlow<MotionData> = _motion.asStateFlow()

    private val motionRepo get() = sensorRepository.motionRepository

    /** Gravity magnitude for G-load (m/s²). */
    private val g = 9.81f

    /** Peak hold decay: reset after this many ms. */
    private var peakHoldUntilMs: Long = 0
    private val peakHoldDurationMs = 3000L

    /** Hard landing: vertical accel threshold (G). */
    private val hardLandingThresholdG = 2.5f

    private var smoothedTurnRateDegPerSec = Float.NaN

    fun startCollecting() {
        combine(
            motionRepo.accelerationFlow(),
            motionRepo.gyroscopeFlow()
        ) { acc, gyro ->
            Pair(acc, gyro)
        }.onEach { (acc, gyro) ->
            val ax = acc.x; val ay = acc.y; val az = acc.z
            val magnitude = sqrt(ax * ax + ay * ay + az * az)
            val gLoad = magnitude / g
            val gPositive = if (gLoad > 1f) gLoad - 1f else 0f
            val gNegative = if (gLoad < 1f) 1f - gLoad else 0f

            var peakPos = _motion.value.gLoadPeakPositive
            var peakNeg = _motion.value.gLoadPeakNegative
            val now = System.currentTimeMillis()
            if (now > peakHoldUntilMs) {
                peakPos = gPositive
                peakNeg = gNegative
                peakHoldUntilMs = now + peakHoldDurationMs
            } else {
                if (gPositive > peakPos) peakPos = gPositive
                if (gNegative > peakNeg) peakNeg = gNegative
            }

            // Turn rate: magnitude of gyro (rad/s -> deg/s), smoothed to reduce shivering
            val turnRateDegPerSec = Math.toDegrees(sqrt(gyro.x * gyro.x + gyro.y * gyro.y + gyro.z * gyro.z).toDouble()).toFloat()
            smoothedTurnRateDegPerSec = if (smoothedTurnRateDegPerSec.isNaN()) turnRateDegPerSec else AviationFormulas.exponentialMovingAverage(turnRateDegPerSec, smoothedTurnRateDegPerSec, 0.75f)

            // Vertical acceleration (device Z in world frame approx; simplified use magnitude change)
            val verticalAccMps2 = abs(az - (-g)) // relative to gravity
            val hardLanding = (magnitude / g) >= (1f + hardLandingThresholdG)

            _motion.value = MotionData(
                turnRateDegPerSec = smoothedTurnRateDegPerSec,
                gLoadPositive = gPositive,
                gLoadNegative = gNegative,
                gLoadPeakPositive = peakPos,
                gLoadPeakNegative = peakNeg,
                verticalAccelerationMps2 = verticalAccMps2,
                hardLandingDetected = hardLanding,
                timestamp = now
            )
        }.launchIn(viewModelScope)
    }
}
