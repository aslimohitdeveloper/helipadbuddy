package com.mskdevelopers.helipadbuddy.data.repository

import android.content.Context
import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Orchestrates weather fetch and widget state persistence.
 */
class WeatherDataFetcher(
    context: Context,
    private val preferencesRepository: PreferencesRepository
) {

    private val weatherRepository = WeatherRepository(context)
    private val weatherRepo = WeatherWidgetRepository()
    private val widgetState = WidgetStateRepository(context)

    suspend fun refresh(
        latitude: Double,
        longitude: Double,
        altitudeMsl: Int,
        sats: Int,
        gpsQuality: String,
        pressure: PressureWidgetData? = null,
        isRefreshing: Boolean = false
    ): WidgetWeatherData = withContext(Dispatchers.IO) {
        if (isRefreshing) {
            widgetState.setRefreshing(true)
        }
        try {
            val previous = widgetState.getCurrent()
            val fetched = weatherRepository.fetchWeather(
                latitude = latitude,
                longitude = longitude,
                altitudeMsl = altitudeMsl,
                sats = sats,
                gpsQuality = gpsQuality,
                pressure = pressure,
                preferredIcao = preferencesRepository.preferredMetarIcao.value
            ).copy(isRefreshing = false)
            val data = WidgetRunwayMerge.applyRunway(
                data = fetched,
                activeRunway = preferencesRepository.activeRunway.value,
                previous = previous
            )
            widgetState.save(data)
            data
        } catch (_: Exception) {
            val fallback = widgetState.getCurrent().copy(
                isRefreshing = false,
                weatherSource = widgetState.getCurrent().weatherSource.ifEmpty {
                    WeatherRepository.WEATHER_SOURCE_COORDINATE
                },
                updatedAtMillis = System.currentTimeMillis()
            )
            widgetState.save(fallback)
            fallback
        }
    }

    suspend fun mergeLivePressure(pressure: PressureWidgetData): WidgetWeatherData = withContext(Dispatchers.IO) {
        val current = widgetState.getCurrent()
        val merged = WidgetRunwayMerge.applyRunway(
            data = weatherRepo.mergePressureIntoWidget(current, pressure),
            activeRunway = preferencesRepository.activeRunway.value,
            previous = current
        )
        widgetState.save(merged)
        merged
    }

    suspend fun incrementRefreshSpinFrame(): Int = withContext(Dispatchers.IO) {
        val current = widgetState.getCurrent()
        val next = (current.refreshSpinFrame + 1) % 4
        widgetState.setRefreshSpinFrame(next)
        next
    }

    suspend fun updatePressureOnly(pressure: PressureWidgetData) {
        withContext(Dispatchers.IO) {
            widgetState.mergePressure(pressure)
        }
    }

    suspend fun markRefreshing(refreshing: Boolean) {
        withContext(Dispatchers.IO) {
            widgetState.setRefreshing(refreshing)
        }
    }

    val weatherFlow get() = widgetState.weatherFlow
}
