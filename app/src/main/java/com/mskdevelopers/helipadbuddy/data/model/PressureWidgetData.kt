package com.mskdevelopers.helipadbuddy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PressureWidgetData(
    val qfeHpa: Float = 0f,
    val qnhHpa: Float = 0f,
    val qffHpa: Float = 0f,
    val temperatureC: Float = 0f,
    val pressureTrend: Float = 0f,
    val trendDirection: TrendDirection = TrendDirection.STABLE
) {
    companion object {
        val EMPTY = PressureWidgetData()
    }
}

@Serializable
enum class TrendDirection {
    RISING,
    FALLING,
    STABLE
}
