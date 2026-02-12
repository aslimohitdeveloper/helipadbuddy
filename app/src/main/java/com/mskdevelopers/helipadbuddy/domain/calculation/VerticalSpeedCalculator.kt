package com.mskdevelopers.helipadbuddy.domain.calculation

/**
 * Vertical speed calculation and smoothing.
 * VSI = ΔAltitude / ΔTime (e.g. ft/min from altitude delta and time delta).
 */
object VerticalSpeedCalculator {

    /** Meters to feet */
    private const val M_TO_FT = 3.28084f

    /** Seconds per minute */
    private const val SEC_PER_MIN = 60f

    /**
     * Vertical speed in ft/min from altitude change (meters) and time delta (seconds).
     */
    fun verticalSpeedFtMin(altitudeDeltaMeters: Float, timeDeltaSeconds: Float): Float {
        if (timeDeltaSeconds <= 0f) return 0f
        val altitudeDeltaFt = altitudeDeltaMeters * M_TO_FT
        return altitudeDeltaFt / timeDeltaSeconds * SEC_PER_MIN
    }

    /**
     * Vertical speed in m/s from altitude change and time delta.
     */
    fun verticalSpeedMps(altitudeDeltaMeters: Float, timeDeltaSeconds: Float): Float {
        if (timeDeltaSeconds <= 0f) return 0f
        return altitudeDeltaMeters / timeDeltaSeconds
    }

    /**
     * Simple moving average of the last N values.
     */
    fun movingAverage(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        return values.average().toFloat()
    }
}
