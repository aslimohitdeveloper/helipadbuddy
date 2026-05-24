package com.mskdevelopers.helipadbuddy.domain.sensor

import kotlin.math.abs
import kotlin.math.sqrt

object SensorDiagnostics {

    enum class Quality { GOOD, FAIR, POOR, UNAVAILABLE }

    data class SensorHealthReport(
        val compassQuality: Quality,
        val compassJitterDeg: Float,
        val magneticStrengthUt: Float,
        val magneticInterference: Boolean,
        val accelerometerQuality: Quality,
        val accelerometerDeviationG: Float,
        val gyroscopeQuality: Quality,
        val gyroDriftDegPerSec: Float,
        val barometerAvailable: Boolean,
        val barometerQuality: Quality
    )

    fun evaluateCompass(headingSamples: List<Float>, magStrength: Float): Pair<Quality, Float> {
        if (headingSamples.size < 3) return Quality.FAIR to 0f
        val jitter = headingSamples.zipWithNext { a, b ->
            abs(normalizeDelta(a, b))
        }.average().toFloat()
        val interference = magStrength > 65f || (magStrength in 0.1f..20f)
        val quality = when {
            interference -> Quality.POOR
            jitter < 3f -> Quality.GOOD
            jitter < 8f -> Quality.FAIR
            else -> Quality.POOR
        }
        return quality to jitter
    }

    fun evaluateAccelerometer(ax: Float, ay: Float, az: Float): Pair<Quality, Float> {
        val g = sqrt(ax * ax + ay * ay + az * az) / 9.81f
        val deviation = abs(g - 1f)
        val quality = when {
            deviation < 0.05f -> Quality.GOOD
            deviation < 0.15f -> Quality.FAIR
            else -> Quality.POOR
        }
        return quality to deviation
    }

    fun evaluateGyro(magnitudeRadSec: Float, atRest: Boolean): Pair<Quality, Float> {
        val deg = Math.toDegrees(magnitudeRadSec.toDouble()).toFloat()
        if (!atRest) return Quality.GOOD to deg
        val quality = when {
            deg < 1f -> Quality.GOOD
            deg < 3f -> Quality.FAIR
            else -> Quality.POOR
        }
        return quality to deg
    }

    private fun normalizeDelta(a: Float, b: Float): Float {
        var d = b - a
        while (d > 180f) d -= 360f
        while (d < -180f) d += 360f
        return d
    }
}
