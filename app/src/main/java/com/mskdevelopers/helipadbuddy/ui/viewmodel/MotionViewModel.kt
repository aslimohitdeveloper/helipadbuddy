package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.MotionData
import com.mskdevelopers.helipadbuddy.data.repository.MotionRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.SensorFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs
import kotlin.math.sqrt

class MotionViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _motion = MutableStateFlow(MotionData.EMPTY)
    val motion: StateFlow<MotionData> = _motion.asStateFlow()

    private val motionRepo get() = sensorRepository.motionRepository

    private val g = 9.81f
    private var peakHoldUntilMs: Long = 0
    private val peakHoldDurationMs = 3000L
    private val hardLandingThresholdG = 2.5f

    private var lowPassTurnRate = Float.NaN
    private var emaTurnRate = Float.NaN
    private var outputTurnRate = 0f
    private var groundSpeedKnots = 0f

    fun setGroundSpeedKnots(knots: Float) {
        groundSpeedKnots = knots
    }

    fun startCollecting() {
        combine(
            motionRepo.accelerationFlow(),
            motionRepo.gyroscopeFlow()
        ) { acc, gyro ->
            Pair(acc, gyro)
        }.onEach { (acc, gyro) ->
            val ax = acc.x
            val ay = acc.y
            val az = acc.z
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

            val gyroMagnitudeRadPerSec = sqrt(gyro.x * gyro.x + gyro.y * gyro.y + gyro.z * gyro.z)
            val rawTurnRateDegPerSec = Math.toDegrees(gyroMagnitudeRadPerSec.toDouble()).toFloat()
            val stabilizedTurnRate = stabilizeTurnRate(rawTurnRateDegPerSec, gyroMagnitudeRadPerSec)

            val verticalAccMps2 = abs(az - (-g))
            val hardLanding = (magnitude / g) >= (1f + hardLandingThresholdG)

            _motion.value = MotionData(
                turnRateDegPerSec = stabilizedTurnRate,
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

    private fun stabilizeTurnRate(rawDegPerSec: Float, gyroMagnitudeRadPerSec: Float): Float {
        if (groundSpeedKnots < 1f && gyroMagnitudeRadPerSec < 0.05f) {
            outputTurnRate = 0f
            lowPassTurnRate = 0f
            emaTurnRate = 0f
            return 0f
        }

        lowPassTurnRate = if (lowPassTurnRate.isNaN()) {
            rawDegPerSec
        } else {
            SensorFilters.lowPass(rawDegPerSec, lowPassTurnRate, alpha = 0.08f)
        }

        var filtered = SensorFilters.applyDeadband(lowPassTurnRate, threshold = 1.0f)

        emaTurnRate = if (emaTurnRate.isNaN()) {
            filtered
        } else {
            SensorFilters.ema(filtered, emaTurnRate, alpha = 0.08f)
        }
        filtered = emaTurnRate

        val limited = SensorFilters.rateLimit(filtered, outputTurnRate, maxChange = 3f)
        var result = limited
        if (kotlin.math.abs(result) < 1f) {
            result = 0f
        }
        outputTurnRate = result
        return result
    }
}
