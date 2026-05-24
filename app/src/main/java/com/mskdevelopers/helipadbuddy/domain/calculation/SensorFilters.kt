package com.mskdevelopers.helipadbuddy.domain.calculation

import kotlin.math.abs

object SensorFilters {

    fun lowPass(current: Float, previous: Float, alpha: Float = 0.15f): Float =
        previous + alpha * (current - previous)

    fun applyDeadband(value: Float, threshold: Float): Float =
        if (abs(value) < threshold) 0f else value

    fun ema(current: Float, previous: Float, alpha: Float): Float =
        previous + alpha * (current - previous)

    fun rateLimit(current: Float, previous: Float, maxChange: Float): Float =
        current.coerceIn(previous - maxChange, previous + maxChange)

    /**
     * Linear regression slope (y per unit x). Returns 0 when insufficient spread.
     */
    fun linearRegressionSlope(samples: List<Pair<Double, Float>>): Float {
        if (samples.size < 3) return 0f
        val xs = samples.map { it.first }
        val ys = samples.map { it.second.toDouble() }
        val xMean = xs.average()
        val yMean = ys.average()
        var numerator = 0.0
        var denominator = 0.0
        for (i in xs.indices) {
            val dx = xs[i] - xMean
            numerator += dx * (ys[i] - yMean)
            denominator += dx * dx
        }
        if (denominator <= 1e-9) return 0f
        return (numerator / denominator).toFloat()
    }
}
