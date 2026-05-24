package com.mskdevelopers.helipadbuddy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ForecastPoint(
    val timeLabel: String = "",
    val temperature: Float = 0f,
    val windDirection: Int = 0,
    val windSpeedKt: Int = 0,
    val precipProbability: Int = 0,
    val cloudCover: Int = 0
)
