package com.mskdevelopers.helipadbuddy.domain.calculation

/**
 * Common aviation conversion formulas.
 */
object AviationFormulas {

    /** m/s to knots */
    const val MPS_TO_KNOTS = 1.94384f

    /** m/s to km/h */
    const val MPS_TO_KMH = 3.6f

    /** meters to feet */
    const val METERS_TO_FEET = 3.28084f

    fun mpsToKnots(mps: Float): Float = mps * MPS_TO_KNOTS
    fun mpsToKmh(mps: Float): Float = mps * MPS_TO_KMH
    fun metersToFeet(meters: Double): Double = meters * METERS_TO_FEET
    fun metersToFeet(meters: Float): Float = meters * METERS_TO_FEET

    /** Normalize angle to [0, 360). */
    fun normalizeDegrees(degrees: Float): Float {
        var d = degrees % 360f
        if (d < 0f) d += 360f
        return d
    }

    /** Track vs heading difference (e.g. for crosswind indication). */
    fun trackVsHeadingDegrees(track: Float, heading: Float): Float =
        normalizeDegrees(normalizeDegrees(track) - normalizeDegrees(heading))

    /** Exponential moving average: smoothed = alpha * previous + (1 - alpha) * current */
    fun exponentialMovingAverage(current: Float, previous: Float, alpha: Float): Float =
        alpha * previous + (1f - alpha) * current

    /** Simple moving average of a list of values. */
    fun movingAverage(values: List<Float>): Float =
        if (values.isEmpty()) 0f else values.average().toFloat()

    /** Average of heading angles, handling wrap-around (e.g., 359° and 1° average to 0°). */
    fun averageHeading(headings: List<Float>): Float {
        if (headings.isEmpty()) return 0f
        var sinSum = 0f
        var cosSum = 0f
        for (h in headings) {
            val rad = Math.toRadians(h.toDouble())
            sinSum += Math.sin(rad).toFloat()
            cosSum += Math.cos(rad).toFloat()
        }
        val avgRad = Math.atan2(sinSum.toDouble(), cosSum.toDouble())
        return normalizeDegrees(Math.toDegrees(avgRad).toFloat())
    }

    /** Convert heading degrees to direction string (N, NNE, NE, ENE, E, etc.). */
    fun degreesToDirection(deg: Float): String {
        val normalized = normalizeDegrees(deg)
        val index = ((normalized + 11.25f) / 22.5f).toInt() % 16
        return when (index) {
            0 -> "N"
            1 -> "NNE"
            2 -> "NE"
            3 -> "ENE"
            4 -> "E"
            5 -> "ESE"
            6 -> "SE"
            7 -> "SSE"
            8 -> "S"
            9 -> "SSW"
            10 -> "SW"
            11 -> "WSW"
            12 -> "W"
            13 -> "WNW"
            14 -> "NW"
            15 -> "NNW"
            else -> "N"
        }
    }
}
