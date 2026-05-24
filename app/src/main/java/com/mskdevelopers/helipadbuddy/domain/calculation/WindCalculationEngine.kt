package com.mskdevelopers.helipadbuddy.domain.calculation

import com.mskdevelopers.helipadbuddy.data.model.WindData
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object WindCalculationEngine {

    private const val MIN_GS_KTS = 15f

    /**
     * Estimate wind from heading, track, and ground speed using wind triangle.
     * Returns EMPTY when ground speed too low for reliable estimate.
     */
    fun calculate(headingDeg: Float, trackDeg: Float, groundSpeedKts: Float): WindData {
        if (groundSpeedKts < MIN_GS_KTS) return WindData.EMPTY

        val headingRad = Math.toRadians(headingDeg.toDouble())
        val trackRad = Math.toRadians(trackDeg.toDouble())
        val gs = groundSpeedKts.toDouble()

        val hx = gs * sin(trackRad)
        val hy = gs * cos(trackRad)
        val ax = gs * sin(headingRad)
        val ay = gs * cos(headingRad)

        val wx = hx - ax
        val wy = hy - ay
        val windSpeed = kotlin.math.sqrt(wx * wx + wy * wy).toFloat()
        var windDir = Math.toDegrees(atan2(wx, wy)).toFloat()
        windDir = AviationFormulas.normalizeDegrees(windDir)

        val angleDiffRad = Math.toRadians((trackDeg - headingDeg).toDouble())
        val crosswind = (groundSpeedKts * sin(angleDiffRad)).toFloat()
        val headwind = (groundSpeedKts * cos(angleDiffRad)).toFloat()

        return WindData(
            directionDeg = windDir.toInt(),
            speedKts = windSpeed,
            crosswindKts = crosswind,
            headwindKts = headwind
        )
    }

    fun formatCrosswind(crosswindKts: Float): String {
        return when {
            abs(crosswindKts) < 0.5f -> "None"
            crosswindKts > 0 -> "%.0f kt Right".format(abs(crosswindKts))
            else -> "%.0f kt Left".format(abs(crosswindKts))
        }
    }

    fun formatHeadwind(headwindKts: Float): String =
        if (headwindKts >= 0.5f) "+%.0f kt".format(headwindKts) else "0 kt"

    fun formatTailwind(tailwindKts: Float): String =
        if (tailwindKts >= 0.5f) "%.0f kt".format(tailwindKts) else "0 kt"
}
