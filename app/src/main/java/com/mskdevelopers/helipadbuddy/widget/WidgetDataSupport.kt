package com.mskdevelopers.helipadbuddy.widget

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.WidgetStateRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

internal const val WEATHER_UNAVAILABLE_MESSAGE = "Weather unavailable"

internal suspend fun loadWidgetData(context: Context): WidgetWeatherData {
    val data = withTimeoutOrNull(3_000L) {
        WidgetStateRepository(context.applicationContext).weatherFlow.first()
    } ?: WidgetWeatherData.EMPTY
    return if (data.shouldShowFallback()) fallbackWidgetData() else data
}

internal fun fallbackWidgetData(): WidgetWeatherData = WidgetWeatherData(
    weather = WEATHER_UNAVAILABLE_MESSAGE,
    weatherSource = "",
    isRefreshing = false,
    updatedAtMillis = 0L
)

internal fun WidgetWeatherData.shouldShowFallback(): Boolean {
    if (isRefreshing) return false
    if (weather == WEATHER_UNAVAILABLE_MESSAGE) return true
    return updatedAtMillis == 0L &&
        windSpeedKt == 0 &&
        temperature == 0f &&
        metarRaw.isBlank() &&
        station.isBlank()
}
