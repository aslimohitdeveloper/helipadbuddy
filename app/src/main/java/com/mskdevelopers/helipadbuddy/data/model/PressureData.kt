package com.mskdevelopers.helipadbuddy.data.model

/**
 * Barometric pressure and derived altitudes.
 * QFE = pressure at field, QNH = sea-level pressure (ICAO formula).
 */
data class PressureData(
    val qfeHpa: Float,
    val qnhHpa: Float,
    val pressureAltitudeMeters: Float,
    val pressureAltitudeFeet: Float,
    val densityAltitudeMeters: Float,
    val densityAltitudeFeet: Float,
    val altitudeTrend10sFtMin: Float,
    val altitudeTrend30sFtMin: Float,
    val trendDirection: TrendDirection,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class TrendDirection {
        CLIMBING,
        DESCENDING,
        LEVEL
    }

    companion object {
        val EMPTY = PressureData(
            qfeHpa = 0f,
            qnhHpa = 0f,
            pressureAltitudeMeters = 0f,
            pressureAltitudeFeet = 0f,
            densityAltitudeMeters = 0f,
            densityAltitudeFeet = 0f,
            altitudeTrend10sFtMin = 0f,
            altitudeTrend30sFtMin = 0f,
            trendDirection = TrendDirection.LEVEL
        )
    }
}
