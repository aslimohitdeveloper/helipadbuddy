package com.mskdevelopers.helipadbuddy.data.repository

import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.TrendDirection
import com.mskdevelopers.helipadbuddy.domain.calculation.AviationFormulas
import com.mskdevelopers.helipadbuddy.domain.calculation.PressureCalculations
import com.mskdevelopers.helipadbuddy.domain.calculation.SensorFilters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Widget-specific pressure pipeline: EMA smoothing, regression-based trend, QFF computation.
 * Internal updates run at sensor rate; visible trend updates every 5 seconds.
 */
class PressureWidgetRepository {

    private val _pressureWidgetData = MutableStateFlow(PressureWidgetData.EMPTY)
    val pressureWidgetData: StateFlow<PressureWidgetData> = _pressureWidgetData.asStateFlow()

    private var smoothedQfe = Float.NaN
    private var internalTrendHpaPerMin = Float.NaN
    private var displayedTrendHpaPerMin = 0f
    private var displayedDirection = TrendDirection.STABLE
    private val qfeHistory = ArrayDeque<Pair<Long, Float>>(120)
    private var lastSampleMs = 0L
    private var lastTrendDisplayMs = 0L

    private var lastPublishedQfe = Float.NaN
    private var lastInternalTemperatureC = 15f

    companion object {
        private const val EMA_ALPHA = 0.85f
        private const val TREND_EMA_ALPHA = 0.08f
        private const val SAMPLE_INTERVAL_MS = 1_000L
        private const val TREND_WINDOW_MIN_MS = 30_000L
        private const val TREND_WINDOW_MAX_MS = 60_000L
        private const val TREND_DISPLAY_INTERVAL_MS = 5_000L
        private const val TREND_STABLE_THRESHOLD_HPA = 0.2f
        const val WIDGET_REFRESH_THRESHOLD_HPA = 0.2f
    }

    fun onPressureSample(
        rawQfeHpa: Float,
        qnhHpa: Float,
        altitudeMslMeters: Double,
        temperatureC: Float
    ): PressureWidgetData {
        if (rawQfeHpa <= 0f) return _pressureWidgetData.value

        val now = System.currentTimeMillis()
        if (now - lastSampleMs < SAMPLE_INTERVAL_MS && !smoothedQfe.isNaN()) {
            return _pressureWidgetData.value
        }
        lastSampleMs = now

        smoothedQfe = if (smoothedQfe.isNaN()) rawQfeHpa
        else AviationFormulas.exponentialMovingAverage(rawQfeHpa, smoothedQfe, EMA_ALPHA)

        qfeHistory.addLast(now to smoothedQfe)
        while (qfeHistory.size > 120) qfeHistory.removeFirst()
        trimHistoryToWindow(now)

        val slopeHpaPerSec = computeRegressionSlopeHpaPerSec(now)
        val slopeHpaPerMin = slopeHpaPerSec * 60f
        internalTrendHpaPerMin = if (internalTrendHpaPerMin.isNaN()) slopeHpaPerMin
        else SensorFilters.ema(slopeHpaPerMin, internalTrendHpaPerMin, TREND_EMA_ALPHA)

        if (now - lastTrendDisplayMs >= TREND_DISPLAY_INTERVAL_MS) {
            val candidate = internalTrendHpaPerMin
            if (abs(candidate - displayedTrendHpaPerMin) >= TREND_STABLE_THRESHOLD_HPA || lastTrendDisplayMs == 0L) {
                displayedTrendHpaPerMin = candidate
                displayedDirection = when {
                    displayedTrendHpaPerMin > TREND_STABLE_THRESHOLD_HPA -> TrendDirection.RISING
                    displayedTrendHpaPerMin < -TREND_STABLE_THRESHOLD_HPA -> TrendDirection.FALLING
                    else -> TrendDirection.STABLE
                }
            }
            lastTrendDisplayMs = now
        }

        val tempForQff = if (temperatureC.isFinite() && temperatureC > -80f) temperatureC else 15f
        lastInternalTemperatureC = tempForQff
        val qff = if (altitudeMslMeters > 0.0) {
            PressureCalculations.calculateQff(
                smoothedQfe.toDouble(),
                altitudeMslMeters,
                tempForQff.toDouble()
            ).toFloat()
        } else smoothedQfe

        val data = PressureWidgetData(
            qfeHpa = smoothedQfe,
            qnhHpa = qnhHpa,
            qffHpa = qff,
            temperatureC = tempForQff,
            pressureTrend = displayedTrendHpaPerMin,
            trendDirection = displayedDirection
        )
        _pressureWidgetData.value = data
        return data
    }

    fun shouldRefreshWidget(currentQfe: Float): Boolean {
        if (lastPublishedQfe.isNaN()) {
            lastPublishedQfe = currentQfe
            return true
        }
        val significant = abs(currentQfe - lastPublishedQfe) >= WIDGET_REFRESH_THRESHOLD_HPA
        if (significant) lastPublishedQfe = currentQfe
        return significant
    }

    fun markWidgetPublished(qfeHpa: Float) {
        lastPublishedQfe = qfeHpa
    }

    internal fun currentTemperatureC(): Float = lastInternalTemperatureC

    private fun trimHistoryToWindow(now: Long) {
        val cutoff = now - TREND_WINDOW_MAX_MS
        while (qfeHistory.size > 2 && qfeHistory.first().first < cutoff) {
            qfeHistory.removeFirst()
        }
    }

    private fun computeRegressionSlopeHpaPerSec(now: Long): Float {
        val minCutoff = now - TREND_WINDOW_MIN_MS
        val samples = qfeHistory.filter { it.first >= minCutoff }
        if (samples.size < 4) return 0f
        val t0 = samples.first().first.toDouble()
        val points = samples.map { (timeMs, pressure) ->
            (timeMs - t0) / 1000.0 to pressure
        }
        return SensorFilters.linearRegressionSlope(points)
    }
}
