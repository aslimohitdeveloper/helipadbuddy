package com.mskdevelopers.helipadbuddy.domain.calculation

import com.mskdevelopers.helipadbuddy.data.model.RunwayWindData
import com.mskdevelopers.helipadbuddy.data.model.RunwayWindSeverity
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RunwayWindCalculator {

    fun calculate(
        windDirection: Int,
        windSpeedKt: Float,
        runwayHeading: Int
    ): RunwayWindData {
        if (windSpeedKt <= 0f) return RunwayWindData.EMPTY

        val angle = normalizeAngle(windDirection - runwayHeading)
        val angleRad = Math.toRadians(angle.toDouble())
        val rawHeadwind = (windSpeedKt * cos(angleRad)).toFloat()
        val rawCrosswind = (windSpeedKt * sin(angleRad)).toFloat()

        val headwindKt = if (rawHeadwind > 0f) rawHeadwind else 0f
        val tailwindKt = if (rawHeadwind < 0f) abs(rawHeadwind) else 0f
        val crosswindDirection = when {
            rawCrosswind > 0.5f -> "Right"
            rawCrosswind < -0.5f -> "Left"
            else -> "None"
        }

        return RunwayWindData(
            crosswindKt = rawCrosswind,
            crosswindDirection = crosswindDirection,
            headwindKt = headwindKt,
            tailwindKt = tailwindKt,
            relativeAngle = angle
        )
    }

    fun crosswindSeverity(crosswindKt: Float): RunwayWindSeverity {
        val magnitude = abs(crosswindKt)
        return when {
            magnitude > 10f -> RunwayWindSeverity.HIGH_CROSSWIND
            magnitude > 5f -> RunwayWindSeverity.MODERATE_CROSSWIND
            else -> RunwayWindSeverity.FAVORABLE
        }
    }

    private fun normalizeAngle(degrees: Int): Float {
        var angle = degrees % 360
        if (angle > 180) angle -= 360
        if (angle < -180) angle += 360
        return angle.toFloat()
    }
}
