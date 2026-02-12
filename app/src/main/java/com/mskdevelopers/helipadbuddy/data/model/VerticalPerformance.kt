package com.mskdevelopers.helipadbuddy.data.model

/**
 * Vertical speed and climb/descent performance.
 * VSI in ft/min, with smoothing applied from barometer.
 */
data class VerticalPerformance(
    val verticalSpeedFtMin: Float,
    val verticalSpeedMps: Float,
    val isClimbing: Boolean,
    val sinkRateWarning: Boolean,
    val smoothedVerticalSpeedFtMin: Float,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = VerticalPerformance(
            verticalSpeedFtMin = 0f,
            verticalSpeedMps = 0f,
            isClimbing = false,
            sinkRateWarning = false,
            smoothedVerticalSpeedFtMin = 0f
        )
    }
}
