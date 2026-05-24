package com.mskdevelopers.helipadbuddy.data.model

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class RunwayConfig(
    val runwayName: String,
    val heading1: Int,
    val heading2: Int,
    val activeRunway: String,
    val runwayLengthMeters: Int? = null,
    val notes: String? = null
) {
    val activeHeadingDeg: Int
        get() = when (activeRunway.trim()) {
            runwayName.substringBefore("/").trim() -> heading1
            runwayName.substringAfter("/").trim() -> heading2
            else -> heading1
        }

    val hasValidHeading: Boolean
        get() = activeHeadingDeg in 0..360

    companion object {
        fun fromIdentifier(
            identifier: String,
            activeEnd: String,
            runwayLengthMeters: Int? = null,
            notes: String? = null
        ): RunwayConfig? {
            val parts = identifier.split("/")
            if (parts.size != 2) return null
            val end1 = parts[0].trim()
            val end2 = parts[1].trim()
            val h1 = end1.toIntOrNull()?.times(10) ?: return null
            val h2 = end2.toIntOrNull()?.times(10) ?: return null
            val active = when (activeEnd.trim()) {
                end1, end2 -> activeEnd.trim()
                else -> end1
            }
            return RunwayConfig(
                runwayName = "$end1/$end2",
                heading1 = h1,
                heading2 = h2,
                activeRunway = active,
                runwayLengthMeters = runwayLengthMeters,
                notes = notes?.trim()?.takeIf { it.isNotEmpty() }
            )
        }
    }
}

@Serializable
data class RunwayWindData(
    val crosswindKt: Float = 0f,
    val crosswindDirection: String = "",
    val headwindKt: Float = 0f,
    val tailwindKt: Float = 0f,
    val relativeAngle: Float = 0f
) {
    val absCrosswindKt: Float get() = abs(crosswindKt)

    companion object {
        val EMPTY = RunwayWindData()
    }
}

enum class RunwayWindSeverity {
    FAVORABLE,
    MODERATE_CROSSWIND,
    HIGH_CROSSWIND
}

enum class RunwayDisplayStatus {
    NO_RUNWAY,
    NO_WIND,
    CONFIGURE_RUNWAY,
    READY
}

data class RunwayDashboardState(
    val status: RunwayDisplayStatus = RunwayDisplayStatus.NO_RUNWAY,
    val activeRunway: RunwayConfig? = null,
    val windDirectionDeg: Int = 0,
    val windSpeedKts: Float = 0f,
    val components: RunwayWindData = RunwayWindData.EMPTY,
    val severity: RunwayWindSeverity = RunwayWindSeverity.FAVORABLE
) {
    companion object {
        val EMPTY = RunwayDashboardState()
    }
}
