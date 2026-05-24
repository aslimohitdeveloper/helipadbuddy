package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.local.FlightDataPoint
import com.mskdevelopers.helipadbuddy.data.repository.LoggingRepository
import com.mskdevelopers.helipadbuddy.domain.sensor.SensorDiagnostics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class SensorHealthViewModel(
    private val sensorRepository: com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
) : ViewModel() {

    private val _report = MutableStateFlow(defaultReport())
    val report: StateFlow<SensorDiagnostics.SensorHealthReport> = _report.asStateFlow()

    private val headingSamples = ArrayDeque<Float>(20)

    fun updateFromSensors(
        heading: Float,
        magStrength: Float,
        ax: Float, ay: Float, az: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
        atRest: Boolean
    ) {
        if (heading != 0f) {
            headingSamples.addLast(heading)
            while (headingSamples.size > 20) headingSamples.removeFirst()
        }
        val (compassQ, jitter) = SensorDiagnostics.evaluateCompass(headingSamples.toList(), magStrength)
        val (accelQ, accelDev) = SensorDiagnostics.evaluateAccelerometer(ax, ay, az)
        val gyroMag = sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)
        val (gyroQ, gyroDrift) = SensorDiagnostics.evaluateGyro(gyroMag, atRest)
        val baroAvail = sensorRepository.hasPressureSensor()
        _report.value = SensorDiagnostics.SensorHealthReport(
            compassQuality = compassQ,
            compassJitterDeg = jitter,
            magneticStrengthUt = magStrength,
            magneticInterference = magStrength > 65f || (magStrength in 0.1f..20f),
            accelerometerQuality = accelQ,
            accelerometerDeviationG = accelDev,
            gyroscopeQuality = gyroQ,
            gyroDriftDegPerSec = gyroDrift,
            barometerAvailable = baroAvail,
            barometerQuality = if (baroAvail) SensorDiagnostics.Quality.GOOD else SensorDiagnostics.Quality.UNAVAILABLE
        )
    }

    private fun defaultReport() = SensorDiagnostics.SensorHealthReport(
        compassQuality = SensorDiagnostics.Quality.FAIR,
        compassJitterDeg = 0f,
        magneticStrengthUt = 0f,
        magneticInterference = false,
        accelerometerQuality = SensorDiagnostics.Quality.FAIR,
        accelerometerDeviationG = 0f,
        gyroscopeQuality = SensorDiagnostics.Quality.FAIR,
        gyroDriftDegPerSec = 0f,
        barometerAvailable = false,
        barometerQuality = SensorDiagnostics.Quality.UNAVAILABLE
    )
}
