package com.mskdevelopers.helipadbuddy.data.model

/**
 * Position and navigation data (WGS84).
 * Used for lat/lon, altitude, ground speed, track, and magnetic heading.
 */
data class PositionData(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val altitudeFeet: Double,
    val groundSpeedMps: Float,
    val groundSpeedKnots: Float,
    val groundSpeedKmh: Float,
    val trackDegrees: Float,
    val headingDegrees: Float,
    val trackVsHeadingDegrees: Float,
    val hasFix: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        val EMPTY = PositionData(
            latitude = 0.0,
            longitude = 0.0,
            altitudeMeters = 0.0,
            altitudeFeet = 0.0,
            groundSpeedMps = 0f,
            groundSpeedKnots = 0f,
            groundSpeedKmh = 0f,
            trackDegrees = 0f,
            headingDegrees = 0f,
            trackVsHeadingDegrees = 0f,
            hasFix = false
        )
    }
}
