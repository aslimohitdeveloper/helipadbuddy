package com.mskdevelopers.helipadbuddy.data.model

/**
 * Motion metrics: turn rate, G-load, and event detection.
 * Turn rate in deg/s, G-load with peak hold, hard landing detection.
 */
data class MotionData(
    val turnRateDegPerSec: Float,
    val gLoadPositive: Float,
    val gLoadNegative: Float,
    val gLoadPeakPositive: Float,
    val gLoadPeakNegative: Float,
    val verticalAccelerationMps2: Float,
    val hardLandingDetected: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = MotionData(
            turnRateDegPerSec = 0f,
            gLoadPositive = 0f,
            gLoadNegative = 0f,
            gLoadPeakPositive = 0f,
            gLoadPeakNegative = 0f,
            verticalAccelerationMps2 = 0f,
            hardLandingDetected = false
        )
    }
}
