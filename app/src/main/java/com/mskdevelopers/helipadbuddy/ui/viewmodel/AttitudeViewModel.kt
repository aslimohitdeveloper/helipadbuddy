package com.mskdevelopers.helipadbuddy.ui.viewmodel

import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.AttitudeData
import com.mskdevelopers.helipadbuddy.data.repository.MotionRepository
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import kotlinx.coroutines.delay
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.math.asin

/**
 * Attitude (pitch, roll) and magnetic heading from accel + gyro + magnetometer.
 * Complementary filter: accel for pitch/roll, magnetometer for heading.
 */
class AttitudeViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _attitude = MutableStateFlow(AttitudeData.EMPTY)
    val attitude: StateFlow<AttitudeData> = _attitude.asStateFlow()

    private val _magneticStrength = MutableStateFlow(0f)
    val magneticStrength: StateFlow<Float> = _magneticStrength.asStateFlow()

    private val motionRepo get() = sensorRepository.motionRepository

    private var lastGyroTimeNs: Long = 0
    private var fusedPitchDeg: Float = 0f
    private var fusedRollDeg: Float = 0f
    private val alpha = 0.98f
    private var smoothedHeadingDeg: Float = 0f
    private val headingAlpha = 0.88f

    fun startCollecting() {
        val magFlow = if (motionRepo.hasMagnetometer()) {
            motionRepo.magneticFlow()
        } else {
            flow {
                while (true) {
                    emit(MotionRepository.Magnetic(1f, 0f, 0f))
                    delay(100)
                }
            }
        }
        combine(
            motionRepo.accelerationFlow(),
            motionRepo.gyroscopeFlow(),
            magFlow
        ) { acc, gyro, mag ->
            Triple(acc, gyro, mag)
        }.onEach { (acc, gyro, mag) ->
            val nowNs = System.nanoTime()
            val dtSec = if (lastGyroTimeNs > 0) (nowNs - lastGyroTimeNs) / 1e9f else 0f
            lastGyroTimeNs = nowNs

            // Pitch/roll from accelerometer (degrees)
            val ax = acc.x; val ay = acc.y; val az = acc.z
            val pitchRad = atan2(-ax, sqrt(ay * ay + az * az))
            val rollRad = atan2(ay, sqrt(ax * ax + az * az))
            val pitchAccDeg = Math.toDegrees(pitchRad.toDouble()).toFloat()
            val rollAccDeg = Math.toDegrees(rollRad.toDouble()).toFloat()

            if (dtSec > 0f && dtSec < 1f) {
                val gyroPitchDeg = Math.toDegrees(gyro.x.toDouble()).toFloat() * dtSec
                val gyroRollDeg = Math.toDegrees(gyro.y.toDouble()).toFloat() * dtSec
                fusedPitchDeg = alpha * (fusedPitchDeg + gyroPitchDeg) + (1f - alpha) * pitchAccDeg
                fusedRollDeg = alpha * (fusedRollDeg + gyroRollDeg) + (1f - alpha) * rollAccDeg
            } else {
                fusedPitchDeg = pitchAccDeg
                fusedRollDeg = rollAccDeg
            }

            // Magnetic heading: rotate mag into horizontal plane using pitch/roll, then atan2
            val sinP = kotlin.math.sin(fusedPitchDeg * Math.PI / 180).toFloat()
            val cosP = kotlin.math.cos(fusedPitchDeg * Math.PI / 180).toFloat()
            val sinR = kotlin.math.sin(fusedRollDeg * Math.PI / 180).toFloat()
            val cosR = kotlin.math.cos(fusedRollDeg * Math.PI / 180).toFloat()
            val hx = mag.x * cosP + mag.y * sinR * sinP + mag.z * cosR * sinP
            val hy = mag.y * cosR - mag.z * sinR
            val headingRad = atan2(hy, hx)
            var headingDeg = Math.toDegrees(headingRad.toDouble()).toFloat()
            // Adjust heading: atan2(hy, hx) gives angle from East, convert to heading from North
            headingDeg = headingDeg + 90f
            headingDeg = AviationFormulas.normalizeDegrees(headingDeg + 180f)

            // Apply EMA smoothing to heading
            if (smoothedHeadingDeg == 0f && headingDeg != 0f) {
                smoothedHeadingDeg = headingDeg
            } else if (headingDeg != 0f) {
                smoothedHeadingDeg = AviationFormulas.exponentialMovingAverage(headingDeg, smoothedHeadingDeg, headingAlpha)
                smoothedHeadingDeg = AviationFormulas.normalizeDegrees(smoothedHeadingDeg)
            }

            // Calculate magnetic field strength (magnitude in μT)
            val magStrength = sqrt(mag.x * mag.x + mag.y * mag.y + mag.z * mag.z)
            // Apply smoothing to magnetic strength
            val smoothedMagStrength = if (_magneticStrength.value == 0f) {
                magStrength
            } else {
                AviationFormulas.exponentialMovingAverage(magStrength, _magneticStrength.value, 0.5f)
            }
            _magneticStrength.value = smoothedMagStrength

            _attitude.value = AttitudeData(
                pitchDegrees = fusedPitchDeg,
                rollDegrees = fusedRollDeg,
                headingDegrees = smoothedHeadingDeg,
                timestamp = System.currentTimeMillis()
            )
        }.launchIn(viewModelScope)
    }
}
