package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.PressureData
import com.mskdevelopers.helipadbuddy.data.repository.SensorRepository
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.domain.calculation.DensityAltitudeCalculator
import com.mskdevelopers.helipadbuddy.domain.calculation.PressureCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Barometric pressure: QFE, QNH, pressure altitude, density altitude, trend.
 */
class PressureViewModel(
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _pressureData = MutableStateFlow(PressureData.EMPTY)
    val pressureData: StateFlow<PressureData> = _pressureData.asStateFlow()

    /** GPS altitude (m) for QNH calculation; set from LocationViewModel. */
    private val _gpsAltitudeMeters = MutableStateFlow(0f)
    fun setGpsAltitudeMeters(alt: Float) {
        _gpsAltitudeMeters.value = alt
    }

    /** User-input field elevation (m) for QNH calculation. If > 0, use this instead of GPS altitude. */
    private val _fieldElevationMeters = MutableStateFlow(0f)
    fun setFieldElevationMeters(elevationMeters: Float) {
        _fieldElevationMeters.value = elevationMeters
    }

    /** OAT °C for density altitude; user input or ISA. */
    private val _oatCelsius = MutableStateFlow(15f)
    val oatCelsius: StateFlow<Float> = _oatCelsius.asStateFlow()
    fun setOatCelsius(celsius: Float) {
        _oatCelsius.value = celsius
    }

    /** Altitude history for 10s/30s trend (simple: last N pressure-derived altitudes). */
    private val altitudeHistory = ArrayDeque<Pair<Long, Float>>(maxOf(30, 10))
    private val pressureToAltitudeMeters: (Float) -> Float = { hpa ->
        PressureCalculations.pressureAltitudeMeters(hpa)
    }

    private var smoothedPaM = Float.NaN
    private var smoothedTrend10 = Float.NaN
    private var smoothedTrend30 = Float.NaN

    fun startCollecting() {
        sensorRepository.pressureRepository.pressureFlow()
            .onEach { qfeHpa ->
                // Use user-input field elevation if set (> 0), otherwise use GPS altitude
                val userElevation = _fieldElevationMeters.value
                val altM = if (userElevation > 0f) userElevation else _gpsAltitudeMeters.value
                val qnh = PressureCalculations.qnhFromQfe(qfeHpa, altM)
                val paM = PressureCalculations.pressureAltitudeMeters(qfeHpa)
                smoothedPaM = if (smoothedPaM.isNaN()) paM else AviationFormulas.exponentialMovingAverage(paM, smoothedPaM, 0.85f)
                val paFt = smoothedPaM * 3.28084f
                val oat = _oatCelsius.value
                val daM = DensityAltitudeCalculator.densityAltitudeMeters(smoothedPaM, oat)
                val daFt = daM * 3.28084f

                val now = System.currentTimeMillis()
                altitudeHistory.addLast(now to paM)
                while (altitudeHistory.size > 60) altitudeHistory.removeFirst()
                val trend10 = trendFtMin(10_000L)
                val trend30 = trendFtMin(30_000L)
                smoothedTrend10 = if (smoothedTrend10.isNaN()) trend10 else AviationFormulas.exponentialMovingAverage(trend10, smoothedTrend10, 0.7f)
                smoothedTrend30 = if (smoothedTrend30.isNaN()) trend30 else AviationFormulas.exponentialMovingAverage(trend30, smoothedTrend30, 0.7f)
                val direction = when {
                    smoothedTrend10 > 50f -> PressureData.TrendDirection.CLIMBING
                    smoothedTrend10 < -50f -> PressureData.TrendDirection.DESCENDING
                    else -> PressureData.TrendDirection.LEVEL
                }

                _pressureData.update {
                    it.copy(
                        qfeHpa = qfeHpa,
                        qnhHpa = qnh,
                        pressureAltitudeMeters = smoothedPaM,
                        pressureAltitudeFeet = paFt,
                        densityAltitudeMeters = daM,
                        densityAltitudeFeet = daFt,
                        altitudeTrend10sFtMin = smoothedTrend10,
                        altitudeTrend30sFtMin = smoothedTrend30,
                        trendDirection = direction,
                        timestamp = now
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun trendFtMin(windowMs: Long): Float {
        if (altitudeHistory.size < 2) return 0f
        val now = altitudeHistory.last().first
        val cutoff = now - windowMs
        val recent = altitudeHistory.filter { it.first >= cutoff }.sortedBy { it.first }
        if (recent.size < 2) return 0f
        val (t0, a0) = recent.first()
        val (t1, a1) = recent.last()
        val dtSec = (t1 - t0) / 1000f
        if (dtSec <= 0f) return 0f
        val altDeltaFt = (a1 - a0) * 3.28084f
        return altDeltaFt / dtSec * 60f
    }
}
