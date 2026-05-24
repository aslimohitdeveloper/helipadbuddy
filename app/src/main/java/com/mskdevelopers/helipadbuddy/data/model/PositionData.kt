package com.mskdevelopers.helipadbuddy.data.model

/**
 * Position and navigation data.
 * altitudeMsl* is mean sea level; altitudeWgs84* is WGS84 ellipsoid height.
 * altitudeMeters/altitudeFeet alias MSL for backward compatibility.
 */
data class PositionData(
    val latitude: Double,
    val longitude: Double,
    val altitudeMslMeters: Double,
    val altitudeWgs84Meters: Double,
    val altitudeMslFeet: Double,
    val altitudeWgs84Feet: Double,
    val groundSpeedMps: Float,
    val groundSpeedKnots: Float,
    val groundSpeedKmh: Float,
    val trackDegrees: Float,
    val headingDegrees: Float,
    val trackVsHeadingDegrees: Float,
    val hasFix: Boolean,
    val fixQuality: String = "NO_FIX",
    val timestamp: Long = System.currentTimeMillis()
) {
  /** MSL altitude alias for logging and QNH callers. */
    val altitudeMeters: Double get() = altitudeMslMeters
    val altitudeFeet: Double get() = altitudeMslFeet

    companion object {
        val EMPTY = PositionData(
            latitude = 0.0,
            longitude = 0.0,
            altitudeMslMeters = 0.0,
            altitudeWgs84Meters = 0.0,
            altitudeMslFeet = 0.0,
            altitudeWgs84Feet = 0.0,
            groundSpeedMps = 0f,
            groundSpeedKnots = 0f,
            groundSpeedKmh = 0f,
            trackDegrees = 0f,
            headingDegrees = 0f,
            trackVsHeadingDegrees = 0f,
            hasFix = false,
            fixQuality = "NO_FIX"
        )
    }
}
