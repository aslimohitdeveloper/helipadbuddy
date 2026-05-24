package com.mskdevelopers.helipadbuddy.ui.viewmodel

import androidx.glance.appwidget.updateAll
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskdevelopers.helipadbuddy.data.model.PressureWidgetData
import com.mskdevelopers.helipadbuddy.data.model.RunwayDashboardState
import com.mskdevelopers.helipadbuddy.data.model.RunwayDisplayStatus
import com.mskdevelopers.helipadbuddy.data.model.WidgetWeatherData
import com.mskdevelopers.helipadbuddy.data.repository.PressureWidgetRepository
import com.mskdevelopers.helipadbuddy.data.repository.WeatherDataFetcher
import com.mskdevelopers.helipadbuddy.data.repository.WidgetStateRepository
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidget
import com.mskdevelopers.helipadbuddy.widget.HelipadWeatherWidgetSmall
import com.mskdevelopers.helipadbuddy.widget.WidgetLocationSnapshot
import com.mskdevelopers.helipadbuddy.widget.WidgetLocationStore
import com.mskdevelopers.helipadbuddy.worker.WorkerScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

class WidgetSyncViewModel(
    private val weatherDataFetcher: WeatherDataFetcher,
    private val pressureWidgetRepository: PressureWidgetRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val _weather = MutableStateFlow(WidgetWeatherData.EMPTY)
    val weather: StateFlow<WidgetWeatherData> = _weather.asStateFlow()

    private var lastLat = 0.0
    private var lastLon = 0.0
    private var lastAltMsl = 0

    init {
        viewModelScope.launch {
            weatherDataFetcher.weatherFlow
                .distinctUntilChanged { old, new ->
                    old.updatedAtMillis == new.updatedAtMillis &&
                        old.isRefreshing == new.isRefreshing &&
                        kotlin.math.abs(old.qfeHpa - new.qfeHpa) < 0.2f &&
                        old.altitudeMsl == new.altitudeMsl
                }
                .collect { _weather.value = it }
        }
    }

    fun syncFromPosition(
        latitude: Double,
        longitude: Double,
        altitudeMslMeters: Double,
        satelliteCount: Int,
        gpsQuality: String
    ) {
        if (!latitude.isFinite() || !longitude.isFinite() || latitude == 0.0 && longitude == 0.0) return
        val altMsl = altitudeMslMeters.toInt()
        viewModelScope.launch {
            WidgetLocationStore.save(
                context,
                WidgetLocationSnapshot(latitude, longitude, altMsl, satelliteCount, gpsQuality)
            )
            val isFirstFix = lastLat == 0.0
            val significantLocation = lastLat != 0.0 && (
                haversineKm(lastLat, lastLon, latitude, longitude) > 5.0 ||
                abs(altMsl - lastAltMsl) > 152
            )
            lastLat = latitude
            lastLon = longitude
            lastAltMsl = altMsl
            if (isFirstFix || significantLocation) {
                WorkerScheduler.enqueueLocationWeatherRefresh(context)
            }
        }
    }

    /**
     * Push live barometer pressure into widget state.
     * DataStore updates every sample; Glance redraw only on significant pressure change.
     */
    fun syncPressure(pressure: PressureWidgetData) {
        if (pressure.qfeHpa <= 0f) return
        viewModelScope.launch {
            weatherDataFetcher.updatePressureOnly(pressure)
            val shouldRedraw = pressureWidgetRepository.shouldRefreshWidget(pressure.qfeHpa)
            if (shouldRedraw) {
                weatherDataFetcher.mergeLivePressure(pressure)
                HelipadWeatherWidget().updateAll(context)
                HelipadWeatherWidgetSmall().updateAll(context)
                pressureWidgetRepository.markWidgetPublished(pressure.qfeHpa)
            }
        }
    }

    fun syncRunwayDashboard(state: RunwayDashboardState, alertSeverity: String = "") {
        viewModelScope.launch {
            val repo = WidgetStateRepository(context)
            val current = repo.getCurrent()
            val side = when (state.components.crosswindDirection) {
                "Right" -> "R"
                "Left" -> "L"
                else -> ""
            }
            val configured = state.activeRunway != null &&
                state.status != RunwayDisplayStatus.NO_RUNWAY
            val merged = current.copy(
                activeRunwayEnd = state.activeRunway?.activeRunway.orEmpty(),
                headwindKt = state.components.headwindKt,
                tailwindKt = state.components.tailwindKt,
                crosswindKt = abs(state.components.crosswindKt),
                crosswindSide = side,
                runwayConfigured = configured,
                alertSeverity = alertSeverity
            )
            repo.save(merged)
            _weather.value = merged
            HelipadWeatherWidget().updateAll(context)
            HelipadWeatherWidgetSmall().updateAll(context)
        }
    }

    fun refreshNow(
        latitude: Double,
        longitude: Double,
        altitudeMsl: Int,
        sats: Int,
        gpsQuality: String,
        pressure: PressureWidgetData? = null
    ) {
        viewModelScope.launch {
            weatherDataFetcher.markRefreshing(true)
            _weather.value = _weather.value.copy(isRefreshing = true)
            try {
                val data = weatherDataFetcher.refresh(
                    latitude, longitude, altitudeMsl, sats, gpsQuality, pressure, isRefreshing = false
                )
                _weather.value = data
            } finally {
                com.mskdevelopers.helipadbuddy.worker.WidgetRefreshCoordinator.finishRefresh(context)
            }
            pressure?.qfeHpa?.let { pressureWidgetRepository.markWidgetPublished(it) }
        }
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        return r * 2 * kotlin.math.asin(kotlin.math.sqrt(a))
    }
}
